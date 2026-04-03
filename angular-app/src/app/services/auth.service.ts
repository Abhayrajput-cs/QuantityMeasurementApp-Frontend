import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL, AUTH_TOKEN_STORAGE_KEY } from '../app.constants';
import { AuthUser } from '../measurement-data';

type LoginResponse = { token: string };
type JwtPayload = {
  sub?: string;
  role?: string;
  exp?: number;
};

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly userState = signal<AuthUser | null>(this.readStoredUser());

  readonly user = computed(() => this.userState());
  readonly isAuthenticated = computed(() => this.userState() !== null);
  readonly isAdmin = computed(() => this.userState()?.role === 'ROLE_ADMIN');

  //LoginCode LOgic
  async login(email: string, password: string): Promise<void> {
  const token = await firstValueFrom(
    this.http.post(`${API_BASE_URL}/api/v1/users/login`, { email, password }, { responseType: 'text' })
  );

  this.storeToken(token);
}
   //Register Code logic
 async register(email: string, password: string): Promise<void> {
  await firstValueFrom(
    this.http.post(`${API_BASE_URL}/api/v1/users/register`, {
      name: email.split('@')[0], 
      email,
      password
    })
  );
}

  storeToken(token: string): void {
    localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token);
    this.userState.set(this.parseToken(token));
  }

  logout(redirect = true): void {
    localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
    this.userState.set(null);
    if (redirect) {
      void this.router.navigate(['/']);
    }
  }

  googleLogin(): void {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
  }

  private readStoredUser(): AuthUser | null {
    const token = localStorage.getItem(AUTH_TOKEN_STORAGE_KEY);
    if (!token) return null;

    try {
      const parsed = this.parseToken(token);
      if (parsed.expiresAt <= Date.now()) {
        localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
        return null;
      }
      return parsed;
    } catch {
      localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
      return null;
    }
  }

private parseToken(token: string): AuthUser {
  const [, payloadPart] = token.split('.');
  const decoded = JSON.parse(atob(payloadPart));

  return {
    email: payloadPart ? decoded.sub : '',
    role: 'ROLE_USER', // ✅ default (backend doesn't send role)
    token,
    expiresAt: decoded.exp * 1000
  };
}
}
