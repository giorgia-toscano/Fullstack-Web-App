import { CommonModule } from '@angular/common';
import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIcon, MatIconModule } from "@angular/material/icon";
import { MatMenuModule } from '@angular/material/menu';
import { MatToolbarModule } from '@angular/material/toolbar';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../services/auth/auth.service';
import { RouterModule } from '@angular/router';
import { UserService } from '../../services/user/user';
import { RealtimeService } from '../../services/realtime/realtime';

@Component({
  selector: 'app-navbar',
  standalone: true,
  imports: [MatIcon,
    CommonModule,         
    MatIconModule,
    MatMenuModule,
    MatButtonModule,
    MatToolbarModule,
    TranslateModule,
    RouterModule,
    
  ],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar implements OnInit{

  authService = inject(AuthService);
  userService = inject(UserService);
  private realtime = inject(RealtimeService);
  private destroyRef = inject(DestroyRef);
  projectCreatedBellOn = signal(false);
  private bellTimer: ReturnType<typeof setTimeout> | null = null;

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) return;

    this.userService.getMe()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(profile => {
        this.userService.currentUser.set(profile);
        const buId = String(profile?.businessUnitId ?? profile?.businessUnit?.idBusinessUnit ?? '').trim();

        if (this.authService.hasRole('ADMIN')) {
          this.realtime.watchAllProjectsCreated();
        } else if (this.authService.hasRole('MANAGER') && buId) {
          this.realtime.watchBuProjectsCreated(buId);
        }
      });

    this.realtime.projectCreated$
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.flashBellGreen());

    this.destroyRef.onDestroy(() => {
      if (this.bellTimer) clearTimeout(this.bellTimer);
      this.realtime.disconnect();
    });
  }

  private flashBellGreen(): void {
    this.projectCreatedBellOn.set(true);
    if (this.bellTimer) clearTimeout(this.bellTimer);
    this.bellTimer = setTimeout(() => this.projectCreatedBellOn.set(false), 3500);
  }

  userInitial = computed(() => {
    const user = this.userService.currentUser();
    return user?.firstName ? user.firstName[0].toUpperCase() : 'U';
  });

  canSeeProjects = computed(() => {
    const roles = this.authService.getRoles();
    if (roles.length > 0) {
      const upper = roles.map(r => String(r).toUpperCase());
      return upper.some(r => r.includes('ADMIN') || r.includes('MANAGER'));
    }

    const profileRole = String(
      this.userService.currentUser()?.roleName ??
      this.userService.currentUser()?.role?.name ??
      ''
    ).toUpperCase();
    return profileRole.includes('ADMIN') || profileRole.includes('MANAGER');
  });
}