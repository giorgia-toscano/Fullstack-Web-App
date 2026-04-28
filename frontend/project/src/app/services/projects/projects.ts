import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ProjectRow } from '../../models/projectRow.model';
import { PageResponse } from '../../models/pageResponse.model';
import { CreateProjectPayload } from '../../models/project.model';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ProjectsService {
  private http = inject(HttpClient);
  private readonly API = `${environment.apiBaseUrl}/projects`;

  list(
    q: string,
    sortField: string,
    sortDir: 'asc' | 'desc',
    filters: {
      bu?: string;
      status?: string;
      startFrom?: string;
      startTo?: string;
      marginMin?: number | null;
      marginMax?: number | null;
      page?: number;
      size?: number;
    } = {}
  ): Observable<PageResponse<ProjectRow>> {
    let params = new HttpParams()
      .set('sortField', sortField)
      .set('sortDir', sortDir);

    if (q?.trim()) params = params.set('q', q.trim());
    if (filters.bu) params = params.set('buId', filters.bu);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.startFrom) params = params.set('startFrom', filters.startFrom);
    if (filters.startTo) params = params.set('startTo', filters.startTo);

    if (filters.marginMin != null) params = params.set('marginMin', String(filters.marginMin));
    if (filters.marginMax != null) params = params.set('marginMax', String(filters.marginMax));

    params = params.set('page', String(filters.page ?? 0));
    params = params.set('size', String(filters.size ?? 10));

    return this.http.get<PageResponse<ProjectRow>>(this.API, { params });
  }

  totals(
    q: string,
    sortField: string,
    sortDir: 'asc' | 'desc',
    filters: {
      bu?: string;
      status?: string;
      startFrom?: string;
      startTo?: string;
      marginMin?: number | null;
      marginMax?: number | null;
    } = {}
  ): Observable<{ count: number; totalRevenue: number; totalCost: number; marginPct: number }> {
    let params = new HttpParams()
      .set('sortField', sortField)
      .set('sortDir', sortDir);

    if (q?.trim()) params = params.set('q', q.trim());
    if (filters.bu) params = params.set('buId', filters.bu);
    if (filters.status) params = params.set('status', filters.status);
    if (filters.startFrom) params = params.set('startFrom', filters.startFrom);
    if (filters.startTo) params = params.set('startTo', filters.startTo);
    if (filters.marginMin != null) params = params.set('marginMin', String(filters.marginMin));
    if (filters.marginMax != null) params = params.set('marginMax', String(filters.marginMax));

    return this.http.get<{ count: number; totalRevenue: number; totalCost: number; marginPct: number }>(
      `${this.API}/totals`,
      { params }
    );
  }

  create(payload: CreateProjectPayload): Observable<unknown> {
    return this.http.post(`${this.API}/create`, payload);
  }

  getAssignableUsers(): Observable<any[]> {
    return this.http.get<any[]>(`${this.API}/assignable-users`);
  }
}