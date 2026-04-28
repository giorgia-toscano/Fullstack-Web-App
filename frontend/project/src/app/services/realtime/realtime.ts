import { Injectable, PLATFORM_ID, inject } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Client } from '../../../../node_modules/@stomp/stompjs/esm6/client.js';
import type { IMessage } from '../../../../node_modules/@stomp/stompjs/esm6/i-message.js';
import type { StompSubscription } from '../../../../node_modules/@stomp/stompjs/esm6/stomp-subscription.js';
import { Subject } from 'rxjs';
import { AuthService } from '../auth/auth.service';
import { ProjectCreatedEvent } from '../../models/projectCreateEvent.model';
import { environment } from '../../../environments/environment';


@Injectable({ 
  providedIn: 'root' 
})
export class RealtimeService {
  private auth = inject(AuthService)
  private platformId = inject(PLATFORM_ID);
  private readonly brokerUrl = this.resolveBrokerUrl();

  private client: Client | null = null;
  private manualDisconnect = false;
  private wantedTopics = new Set<string>();
  private subs = new Map<string, StompSubscription>();
  private projectCreatedSubject = new Subject<ProjectCreatedEvent>();
  projectCreated$ = this.projectCreatedSubject.asObservable();

  connect() {
    if (!isPlatformBrowser(this.platformId)) return;
    if (this.client) return;
    const token = this.auth.getAccessToken();
    if (!token) return;

    this.manualDisconnect = false;

    this.client = new Client({
      brokerURL: this.brokerUrl,
      reconnectDelay: 3000,
      connectHeaders: { Authorization: `Bearer ${token}` },
      beforeConnect: async () => {
        const latestToken = this.auth.getAccessToken();
        if (!latestToken) {
          this.stopReconnect('missing access token');
          throw new Error('NO_ACCESS_TOKEN');
        }
        this.client!.connectHeaders = { Authorization: `Bearer ${latestToken}` };
      }
    });

    this.client.onConnect = () => this.wantedTopics.forEach(t => this.subscribeTopic(t));
    this.client.onStompError = (frame: { headers: Record<string, string>; body?: string }) => {
      console.warn('STOMP error', frame);

      const details = `${frame.headers['message'] ?? ''} ${frame.body ?? ''}`.toUpperCase();
      const authError =
        details.includes('NO_TOKEN') ||
        details.includes('INVALID_TOKEN') ||
        details.includes('NOT_AUTHENTICATED') ||
        details.includes('ACCESS_DENIED') ||
        details.includes('DEST_NOT_ALLOWED') ||
        details.includes('BU_NOT_ALLOWED');

      if (authError) {
        this.stopReconnect(`stomp auth error: ${details}`);
      }
    };
    this.client.onWebSocketError = (evt: Event) => console.warn('WS error', evt);
    this.client.onWebSocketClose = (evt: CloseEvent) => {
      console.warn('WS closed', evt.code, evt.reason);
      if (this.manualDisconnect) return;

      if (!this.auth.getAccessToken()) {
        this.stopReconnect('websocket closed without access token');
      }
    };
    this.client.activate();
  }

  watchBuProjectsCreated(buId: string) {
    const topic = `/topic/bu/${buId}/projects.created`;
    this.wantedTopics.add(topic);
    this.connect();
    this.subscribeTopic(topic);
  }

  watchAllProjectsCreated() {
    const topic = '/topic/projects.created';
    this.wantedTopics.add(topic);
    this.connect();
    this.subscribeTopic(topic);
  }

  clearBuSubscriptions() {
    this.subs.forEach(s => s.unsubscribe());
    this.subs.clear();
    this.wantedTopics.clear();
  }

  disconnect() {
    this.manualDisconnect = true;
    this.clearBuSubscriptions();
    const client = this.client;
    if (!client) return;
    void client.deactivate().finally(() => {
      if (this.client === client) {
        this.client = null;
      }
    });
  }

  private subscribeTopic(topic: string) {
    if (!this.client?.connected || this.subs.has(topic)) return;
    const sub = this.client.subscribe(topic, (msg: IMessage) => {
      try { this.projectCreatedSubject.next(JSON.parse(msg.body)); } catch {}
    });
    this.subs.set(topic, sub);
  }

  private stopReconnect(reason: string) {
    const client = this.client;
    if (!client) return;
    console.warn('Realtime reconnect stopped:', reason);
    client.reconnectDelay = 0;
    this.manualDisconnect = true;
    void client.deactivate().finally(() => {
      if (this.client === client) {
        this.client = null;
      }
    });
  }

  private resolveBrokerUrl(): string {
    const wsBase = environment.wsBaseUrl.trim().replace(/\/+$/, '');
    if (wsBase) {
      return `${wsBase}/ws`;
    }

    const apiBase = environment.apiBaseUrl.trim().replace(/\/+$/, '');
    if (apiBase.startsWith('https://')) {
      return `wss://${apiBase.slice('https://'.length)}/ws`;
    }
    if (apiBase.startsWith('http://')) {
      return `ws://${apiBase.slice('http://'.length)}/ws`;
    }

    if (isPlatformBrowser(this.platformId)) {
      const wsProtocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
      return `${wsProtocol}://${window.location.host}/ws`;
    }

    return 'ws://localhost:8080/ws';
  }
}