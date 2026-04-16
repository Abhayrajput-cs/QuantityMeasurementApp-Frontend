import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { API_BASE_URL, AUTH_TOKEN_STORAGE_KEY } from '../app.constants';
import { AuthUser } from '../measurement-data';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly userState = signal<AuthUser | null>(this.readStoredUser());

  readonly user = computed(() => this.userState());
  readonly isAuthenticated = computed(() => this.userState() !== null);
  readonly isAdmin = computed(() => this.userState()?.role === 'ROLE_ADMIN');

  // ================= EMAIL REGEX =================
  private isValidEmail(email: string): boolean {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  }

  // ================= PASSWORD VALIDATION =================
  private isValidPassword(password: string): boolean {
    return password.length >= 6;
  }

  // ================= LOGIN =================
  async login(email: string, password: string): Promise<void> {

    if (!email || !password) {
      alert("Email and password are required");
      return;
    }

    if (!this.isValidEmail(email)) {
      alert("Enter a valid email");
      return;
    }

    try {
      const token = await firstValueFrom(
        this.http.post(
          `${API_BASE_URL}/api/v1/users/login`,
          { email, password },
          { responseType: 'text' }
        )
      );

      this.storeToken(token);

    } catch (error: any) {

      let message = "Something went wrong";

      if (typeof error.error === 'string') {
        message = error.error;
      } else if (error.status === 0) {
        message = "Server not reachable";
      }

      alert(message);
    }
  }

  // ================= REGISTER =================
  async register(email: string, password: string): Promise<void> {

  // ✅ CLEAR OLD SESSION (VERY IMPORTANT)
  localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
  localStorage.removeItem('userId');
  this.userState.set(null);

  if (!email || !password) {
    alert("Email and password required");
    return;
  }

  if (!this.isValidEmail(email)) {
    alert("Enter a valid email (example: user@gmail.com)");
    return;
  }

  if (!this.isValidPassword(password)) {
    alert("Password must be at least 6 characters");
    return;
  }

  try {
    const res = await firstValueFrom(
      this.http.post(
        `${API_BASE_URL}/api/v1/users/register`,
        {
          name: email.split('@')[0],
          email,
          password
        },
        { responseType: 'text' }
      )
    );

    alert("Registration successful. Please login.");

    await this.router.navigate(['/login']);

  } catch (error: any) {

    if (error.status === 400) {
      alert(error.error);
    } else {
      alert("Registration failed");
    }
  }
}
  // ================= GOOGLE LOGIN =================
  googleLogin(): void {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`;
  }

  // ================= TOKEN =================
  storeToken(token: string): void {

    if (!token || token.split('.').length !== 3) {
      console.error("Invalid token");
      return;
    }

    localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, token);
    this.userState.set(this.parseToken(token));
  }

  logout(): void {
    localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY);
    localStorage.removeItem('userId');
    this.userState.set(null);
    void this.router.navigate(['/']);
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
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');
    const decoded = JSON.parse(atob(base64));

    const userId = decoded.userId || decoded.id;

    if (userId) {
      localStorage.setItem('userId', userId.toString());
    }

    return {
      email: decoded.sub || '',
      role: decoded.role || 'ROLE_USER',
      token,
      expiresAt: decoded.exp * 1000
    };
  }
}