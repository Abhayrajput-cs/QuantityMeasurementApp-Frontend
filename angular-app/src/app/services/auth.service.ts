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
//   async login(email: string, password: string): Promise<void> {
//   try {
//     const token = await firstValueFrom(
//       this.http.post(
//         `${API_BASE_URL}/api/v1/users/login`,
//         { email, password },
//         { responseType: 'text' }
//       )
//     );

//     this.storeToken(token); // ✅ only runs if success

//   } catch (error: any) {
//     console.error("Login failed:", error);

//     // 🔥 show backend message
//     const message = error?.error || "Login failed";
//     alert(message);

//     // ❌ DO NOT call storeToken here
//   }
// }


async login(email: string, password: string): Promise<void> {

  // 🔥 FRONTEND VALIDATION
  if (!email || !password) {
    alert("Email and password are required");
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

    console.log("ERROR FULL:", error);

    let message = "Something went wrong";

    if (typeof error.error === 'string') {
      message = error.error;
    } else if (error.status === 0) {
      message = "Email or password incorrect, or server not reachable";
    }

    alert(message);
  }
}
private isValidEmail(email: string): boolean {
  const emailRegex = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$/;
  return emailRegex.test(email);
}

private isValidPassword(password: string): boolean {
  const passwordRegex =
    /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
  return passwordRegex.test(password);
}
   //Register Code logic
async register(email: string, password: string): Promise<void> {

  if (!email || !password) {
    alert("Email and password required");
    return;
  }

  if (!this.isValidEmail(email)) {
    alert("Invalid email format");
    return;
  }

  if (!this.isValidPassword(password)) {
    alert("Password must be 8+ chars with upper, lower, number, special char");
    return;
  }

  try {
    const res = await firstValueFrom(
      this.http.post(`${API_BASE_URL}/api/v1/users/register`, {
        name: email.split('@')[0],
        email,
        password
      }, { responseType: 'text' })
    );

    alert(res);

  } catch (error: any) {
    alert(error?.error || "Registration failed");
  }
}


 storeToken(token: string): void {
  if (!token || token.split('.').length !== 3) {
    console.error("Invalid token received:", token);
    return; // 🔥 STOP here
  }

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
window.location.href = "http://localhost:8080/oauth2/authorization/google";  }

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
  if (!token || token.split('.').length !== 3) {
    throw new Error('Invalid JWT token');
  }

  const [, payloadPart] = token.split('.');

  // 🔥 Base64URL → Base64
  const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/');

  const decoded = JSON.parse(atob(base64));

  return {
    email: decoded.sub || '',
    role: 'ROLE_USER',
    token,
    expiresAt: decoded.exp * 1000
  };
}
}
