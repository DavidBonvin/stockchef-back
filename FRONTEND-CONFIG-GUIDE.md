# 🌐 Configuración Frontend - Dual Backend (Railway + Render)

## 📋 Objetivo
Configurar tu frontend para que pueda usar **Railway** como backend principal y **Render** como respaldo automático (failover).

---

## 🎯 Opción 1: Backend Único (Más Simple)

### Para React/Next.js/Vite

#### 1. Crear archivo de configuración
```typescript
// src/config/api.ts
const API_BASE_URL = process.env.REACT_APP_API_URL || 
  process.env.NEXT_PUBLIC_API_URL ||
  'https://stockchef-back-production.up.railway.app/api';

export default API_BASE_URL;
```

#### 2. Variables de entorno

**`.env.production`** (para producción)
```bash
# Usar Railway como principal
REACT_APP_API_URL=https://stockchef-back-production.up.railway.app/api
# O para Next.js:
NEXT_PUBLIC_API_URL=https://stockchef-back-production.up.railway.app/api
```

**`.env.development`** (para desarrollo local)
```bash
REACT_APP_API_URL=http://localhost:8090/api
# O para Next.js:
NEXT_PUBLIC_API_URL=http://localhost:8090/api
```

**`.env.local.render`** (backup con Render)
```bash
# Si Railway falla, cambiar a Render
REACT_APP_API_URL=https://stockchef-back.onrender.com/api
```

#### 3. Uso en componentes
```typescript
import API_BASE_URL from './config/api';

// Ejemplo de login
async function login(email: string, password: string) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });
  
  return response.json();
}
```

---

## 🔄 Opción 2: Failover Automático (Avanzado)

### Sistema de respaldo automático si Railway falla

#### 1. Configuración con múltiples backends
```typescript
// src/config/api.ts

export const API_ENDPOINTS = {
  primary: 'https://stockchef-back-production.up.railway.app/api',
  fallback: 'https://stockchef-back.onrender.com/api',
  local: 'http://localhost:8090/api'
};

// Detectar entorno
const getCurrentEndpoint = () => {
  if (process.env.NODE_ENV === 'development') {
    return API_ENDPOINTS.local;
  }
  
  // En producción, usar primary por defecto
  return API_ENDPOINTS.primary;
};

export const API_BASE_URL = getCurrentEndpoint();
export default API_BASE_URL;
```

#### 2. Función de fetch con failover automático
```typescript
// src/utils/fetchWithFallback.ts

import { API_ENDPOINTS } from '../config/api';

interface FetchOptions extends RequestInit {
  timeout?: number;
}

/**
 * Realiza fetch con failover automático
 * Intenta primary primero, si falla usa fallback
 */
export async function fetchWithFallback(
  endpoint: string,
  options: FetchOptions = {}
): Promise<Response> {
  const { timeout = 10000, ...fetchOptions } = options;
  
  // Helper para fetch con timeout
  const fetchWithTimeout = async (url: string): Promise<Response> => {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), timeout);
    
    try {
      const response = await fetch(url, {
        ...fetchOptions,
        signal: controller.signal,
      });
      clearTimeout(timeoutId);
      return response;
    } catch (error) {
      clearTimeout(timeoutId);
      throw error;
    }
  };

  // Intentar con Railway (primary)
  try {
    console.log('🚀 Intentando Railway...');
    const response = await fetchWithTimeout(
      `${API_ENDPOINTS.primary}${endpoint}`
    );
    
    if (response.ok) {
      console.log('✅ Railway respondió correctamente');
      return response;
    }
    
    throw new Error(`Railway responded with ${response.status}`);
  } catch (error) {
    console.warn('⚠️ Railway falló, intentando Render...', error);
    
    // Intentar con Render (fallback)
    try {
      const response = await fetchWithTimeout(
        `${API_ENDPOINTS.fallback}${endpoint}`
      );
      
      if (response.ok) {
        console.log('✅ Render respondió correctamente (fallback)');
        return response;
      }
      
      throw new Error(`Render responded with ${response.status}`);
    } catch (fallbackError) {
      console.error('❌ Ambos backends fallaron', fallbackError);
      throw new Error('Todos los backends están caídos. Intenta más tarde.');
    }
  }
}

/**
 * Helper para requests JSON con failover
 */
export async function fetchJsonWithFallback<T = any>(
  endpoint: string,
  options: FetchOptions = {}
): Promise<T> {
  const response = await fetchWithFallback(endpoint, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });
  
  return response.json();
}
```

#### 3. Servicio de autenticación con failover
```typescript
// src/services/authService.ts

import { fetchJsonWithFallback } from '../utils/fetchWithFallback';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  email: string;
  fullName: string;
  role: string;
  expiresIn: number;
}

export interface RegisterData {
  fullName: string;
  email: string;
  password: string;
}

export const authService = {
  async login(credentials: LoginCredentials): Promise<LoginResponse> {
    try {
      return await fetchJsonWithFallback<LoginResponse>('/auth/login', {
        method: 'POST',
        body: JSON.stringify(credentials),
      });
    } catch (error) {
      console.error('Error en login:', error);
      throw error;
    }
  },

  async register(data: RegisterData): Promise<LoginResponse> {
    try {
      return await fetchJsonWithFallback<LoginResponse>('/auth/register', {
        method: 'POST',
        body: JSON.stringify(data),
      });
    } catch (error) {
      console.error('Error en registro:', error);
      throw error;
    }
  },

  async getCurrentUser(token: string): Promise<any> {
    try {
      return await fetchJsonWithFallback('/auth/me', {
        headers: {
          'Authorization': `Bearer ${token}`,
        },
      });
    } catch (error) {
      console.error('Error obteniendo usuario:', error);
      throw error;
    }
  },
};
```

#### 4. Uso en componentes React
```typescript
// src/components/Login.tsx

import React, { useState } from 'react';
import { authService } from '../services/authService';

export const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await authService.login({ email, password });
      
      // Guardar token
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify({
        email: response.email,
        fullName: response.fullName,
        role: response.role,
      }));
      
      // Redirigir o actualizar estado
      console.log('✅ Login exitoso:', response);
      
    } catch (err: any) {
      setError(err.message || 'Error al iniciar sesión');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <h2>Iniciar Sesión</h2>
      
      {error && <div className="error">{error}</div>}
      
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="Email"
        required
      />
      
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="Contraseña"
        required
      />
      
      <button type="submit" disabled={loading}>
        {loading ? 'Cargando...' : 'Ingresar'}
      </button>
    </form>
  );
};
```

---

## 🔧 Opción 3: Selector Manual de Backend

### Para poder cambiar manualmente entre Railway y Render

#### 1. Configuración con selector
```typescript
// src/config/api.ts

export type BackendProvider = 'railway' | 'render' | 'local';

export const API_ENDPOINTS: Record<BackendProvider, string> = {
  railway: 'https://stockchef-back-production.up.railway.app/api',
  render: 'https://stockchef-back.onrender.com/api',
  local: 'http://localhost:8090/api',
};

// Obtener el backend actual desde localStorage o usar railway por defecto
export const getCurrentBackend = (): BackendProvider => {
  if (typeof window === 'undefined') return 'railway';
  
  const saved = localStorage.getItem('backend-provider') as BackendProvider;
  return saved || 'railway';
};

// Cambiar el backend
export const setBackend = (provider: BackendProvider): void => {
  localStorage.setItem('backend-provider', provider);
  window.location.reload(); // Recargar la app
};

// URL base actual
export const getApiBaseUrl = (): string => {
  return API_ENDPOINTS[getCurrentBackend()];
};
```

#### 2. Componente selector de backend
```typescript
// src/components/BackendSelector.tsx

import React, { useState } from 'react';
import { 
  getCurrentBackend, 
  setBackend, 
  BackendProvider 
} from '../config/api';

export const BackendSelector: React.FC = () => {
  const [current, setCurrent] = useState<BackendProvider>(getCurrentBackend());

  const handleChange = (provider: BackendProvider) => {
    if (confirm(`¿Cambiar a ${provider.toUpperCase()}? La página se recargará.`)) {
      setBackend(provider);
    }
  };

  return (
    <div className="backend-selector">
      <label>Servidor Backend:</label>
      <select 
        value={current} 
        onChange={(e) => handleChange(e.target.value as BackendProvider)}
      >
        <option value="railway">Railway (Principal)</option>
        <option value="render">Render (Respaldo)</option>
        <option value="local">Local (Desarrollo)</option>
      </select>
      
      <span className="current-status">
        Actual: {current.toUpperCase()}
      </span>
    </div>
  );
};
```

---

## 📱 Para Ionic React

### Configuración en Ionic

#### 1. Archivo de configuración
```typescript
// src/config/api.config.ts

import { isPlatform } from '@ionic/react';

export const API_CONFIG = {
  production: {
    primary: 'https://stockchef-back-production.up.railway.app/api',
    fallback: 'https://stockchef-back.onrender.com/api',
  },
  development: {
    primary: 'http://localhost:8090/api',
    fallback: 'http://localhost:8090/api',
  }
};

export const getApiUrl = (): string => {
  const isDev = process.env.NODE_ENV === 'development';
  const config = isDev ? API_CONFIG.development : API_CONFIG.production;
  
  // En dispositivo móvil, siempre usar producción
  if (isPlatform('hybrid')) {
    return API_CONFIG.production.primary;
  }
  
  return config.primary;
};
```

#### 2. Uso con Capacitor HTTP
```typescript
// src/services/api.service.ts

import { CapacitorHttp, HttpResponse } from '@capacitor/core';
import { getApiUrl } from '../config/api.config';

export const apiService = {
  async post<T = any>(endpoint: string, data: any): Promise<T> {
    const options = {
      url: `${getApiUrl()}${endpoint}`,
      headers: { 'Content-Type': 'application/json' },
      data: data,
    };

    const response: HttpResponse = await CapacitorHttp.post(options);
    return response.data;
  },

  async get<T = any>(endpoint: string, token?: string): Promise<T> {
    const headers: any = { 'Content-Type': 'application/json' };
    
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
      url: `${getApiUrl()}${endpoint}`,
      headers,
    };

    const response: HttpResponse = await CapacitorHttp.get(options);
    return response.data;
  },
};
```

---

## ✅ Checklist de Implementación

- [ ] Archivo `api.ts` o `api.config.ts` creado
- [ ] Variables de entorno configuradas (`.env.production`, `.env.development`)
- [ ] Función `fetchWithFallback` implementada (si usas failover)
- [ ] Servicios de autenticación actualizados
- [ ] Componentes usando la nueva configuración
- [ ] Probado en desarrollo local
- [ ] Probado con Railway
- [ ] Probado con Render (fallback)
- [ ] Deploy del frontend actualizado

---

## 🧪 Pruebas

### 1. Probar conexión a Railway
```bash
# En consola del navegador
fetch('https://stockchef-back-production.up.railway.app/api/health')
  .then(r => r.json())
  .then(console.log);
```

### 2. Probar failover
- Desconecta WiFi temporalmente
- Debería cambiar automáticamente a Render
- O mostrar mensaje de error si ambos fallan

---

## 💡 Recomendaciones

1. **Usar Railway como principal**: Es más barato y rápido
2. **Render como respaldo**: Solo mientras migrass completamente
3. **Implementar failover**: Para máxima disponibilidad
4. **Monitorear**: Logs para ver cuál backend se está usando
5. **Cache**: Guardar en localStorage el último backend que funcionó

---

*¡Listo! Tu frontend ahora puede usar ambos backends con facilidad.* 🚀
