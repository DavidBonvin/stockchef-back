# 📱 **GUÍA FRONTEND: IONIC + REACT PARA STOCKCHEF**

## 🎯 **¿POR QUÉ IONIC + REACT?**

```typescript
✅ MULTIPLATAFORMA: Web, iOS, Android desde un código
✅ REACT: Tecnología que ya conoces
✅ UI COMPONENTS: Diseño móvil profesional automático  
✅ CAPACITOR: Acceso nativo (cámara, GPS, etc.)
✅ PWA: Funciona offline automáticamente
✅ DEPLOY FÁCIL: Vercel gratuito
```

## 🚀 **SETUP COMPLETO PASO A PASO**

### **1. INSTALACIÓN INICIAL**
```bash
# Instalar herramientas globales
npm install -g @ionic/cli @capacitor/cli

# Crear proyecto StockChef Frontend
ionic start stockchef-front react --type=react --capacitor
cd stockchef-front

# Dependencias para API y estado
npm install axios @tanstack/react-query 
npm install @ionic/react @ionic/react-router
npm install react-hook-form @hookform/resolvers yup
```

### **2. ESTRUCTURA DE PROYECTO**
```
src/
├── components/
│   ├── Auth/
│   │   ├── LoginForm.tsx
│   │   └── ProtectedRoute.tsx
│   ├── Layout/
│   │   ├── Header.tsx
│   │   ├── Menu.tsx
│   │   └── TabBar.tsx
│   └── Common/
│       ├── Loading.tsx
│       └── ErrorMessage.tsx
├── pages/
│   ├── Login.tsx
│   ├── Dashboard.tsx
│   ├── Inventory/
│   ├── Suppliers/
│   └── Reports/
├── services/
│   ├── api.ts
│   ├── auth.service.ts
│   └── inventory.service.ts
├── hooks/
│   ├── useAuth.ts
│   └── useApi.ts
├── types/
│   └── api.types.ts
└── utils/
    ├── constants.ts
    └── helpers.ts
```

### **3. CONFIGURACIÓN API SERVICE**

```typescript
// src/services/api.ts
import axios from 'axios';

const API_BASE_URL = process.env.NODE_ENV === 'production'
  ? 'https://stockchef-back-production.up.railway.app/api'
  : 'http://localhost:8090/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para JWT
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para errores
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('jwt_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### **4. SERVICIO DE AUTENTICACIÓN**

```typescript
// src/services/auth.service.ts
import api from './api';

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: {
    id: number;
    email: string;
    firstName: string;
    lastName: string;
    role: string;
  };
}

export const authService = {
  async login(credentials: LoginRequest): Promise<LoginResponse> {
    const response = await api.post('/auth/login', credentials);
    
    // Guardar token
    localStorage.setItem('jwt_token', response.data.token);
    
    return response.data;
  },

  logout() {
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_data');
  },

  isAuthenticated(): boolean {
    return !!localStorage.getItem('jwt_token');
  },

  getCurrentUser() {
    const userData = localStorage.getItem('user_data');
    return userData ? JSON.parse(userData) : null;
  }
};
```

### **5. HOOK PERSONALIZADO PARA AUTH**

```typescript
// src/hooks/useAuth.ts
import { useState, useContext, createContext } from 'react';
import { authService, LoginRequest } from '../services/auth.service';

interface AuthContextType {
  user: any;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState(authService.getCurrentUser());
  const [isLoading, setIsLoading] = useState(false);

  const login = async (credentials: LoginRequest) => {
    setIsLoading(true);
    try {
      const response = await authService.login(credentials);
      setUser(response.user);
      localStorage.setItem('user_data', JSON.stringify(response.user));
    } finally {
      setIsLoading(false);
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};
```

### **6. COMPONENTE LOGIN**

```typescript
// src/pages/Login.tsx
import { IonContent, IonPage, IonButton, IonInput, IonItem, IonLabel, IonToast } from '@ionic/react';
import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';
import { useHistory } from 'react-router-dom';

const Login: React.FC = () => {
  const [email, setEmail] = useState('developer@stockchef.com');
  const [password, setPassword] = useState('devpass123');
  const [showToast, setShowToast] = useState(false);
  const [toastMessage, setToastMessage] = useState('');
  
  const { login, isLoading } = useAuth();
  const history = useHistory();

  const handleLogin = async () => {
    try {
      await login({ email, password });
      history.replace('/dashboard');
    } catch (error) {
      setToastMessage('Error de autenticación');
      setShowToast(true);
    }
  };

  return (
    <IonPage>
      <IonContent className="ion-padding">
        <div className="login-container">
          <h1>🍽️ StockChef</h1>
          <p>Gestión Inteligente de Inventarios</p>
          
          <IonItem>
            <IonLabel position="floating">Email</IonLabel>
            <IonInput
              value={email}
              onIonInput={(e) => setEmail(e.detail.value!)}
              type="email"
            />
          </IonItem>

          <IonItem>
            <IonLabel position="floating">Contraseña</IonLabel>
            <IonInput
              value={password}
              onIonInput={(e) => setPassword(e.detail.value!)}
              type="password"
            />
          </IonItem>

          <IonButton 
            expand="block" 
            onClick={handleLogin}
            disabled={isLoading}
            className="ion-margin-top"
          >
            {isLoading ? 'Iniciando sesión...' : 'Iniciar Sesión'}
          </IonButton>
        </div>

        <IonToast
          isOpen={showToast}
          onDidDismiss={() => setShowToast(false)}
          message={toastMessage}
          duration={3000}
          color="danger"
        />
      </IonContent>
    </IonPage>
  );
};

export default Login;
```

### **7. DASHBOARD PRINCIPAL**

```typescript
// src/pages/Dashboard.tsx
import { IonContent, IonPage, IonHeader, IonToolbar, IonTitle, IonCard, IonCardContent, IonCardHeader, IonCardTitle, IonGrid, IonRow, IonCol, IonIcon } from '@ionic/react';
import { cubeOutline, peopleOutline, statsChartOutline, alertCircleOutline } from 'ionicons/icons';
import { useAuth } from '../hooks/useAuth';

const Dashboard: React.FC = () => {
  const { user } = useAuth();

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonTitle>Dashboard - StockChef</IonTitle>
        </IonToolbar>
      </IonHeader>
      
      <IonContent className="ion-padding">
        <div className="welcome-section">
          <h2>¡Bienvenido, {user?.firstName}!</h2>
          <p>Rol: {user?.role}</p>
        </div>

        <IonGrid>
          <IonRow>
            <IonCol size="12" sizeMd="6">
              <IonCard>
                <IonCardHeader>
                  <IonCardTitle>
                    <IonIcon icon={cubeOutline} /> Inventario
                  </IonCardTitle>
                </IonCardHeader>
                <IonCardContent>
                  Gestionar productos y stock
                </IonCardContent>
              </IonCard>
            </IonCol>
            
            <IonCol size="12" sizeMd="6">
              <IonCard>
                <IonCardHeader>
                  <IonCardTitle>
                    <IonIcon icon={peopleOutline} /> Proveedores
                  </IonCardTitle>
                </IonCardHeader>
                <IonCardContent>
                  Administrar proveedores
                </IonCardContent>
              </IonCard>
            </IonCol>
          </IonRow>
        </IonGrid>
      </IonContent>
    </IonPage>
  );
};

export default Dashboard;
```

### **8. CONFIGURACIÓN APP PRINCIPAL**

```typescript
// src/App.tsx
import { Redirect, Route } from 'react-router-dom';
import { IonApp, IonRouterOutlet, setupIonicReact } from '@ionic/react';
import { IonReactRouter } from '@ionic/react-router';
import { AuthProvider, useAuth } from './hooks/useAuth';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';

/* Core CSS required for Ionic components to work properly */
import '@ionic/react/css/core.css';
import '@ionic/react/css/normalize.css';
import '@ionic/react/css/structure.css';
import '@ionic/react/css/typography.css';
import '@ionic/react/css/ionic.swiper.css';

/* Optional CSS utils that can be commented out */
import '@ionic/react/css/padding.css';
import '@ionic/react/css/float-elements.css';
import '@ionic/react/css/text-alignment.css';
import '@ionic/react/css/text-transformation.css';
import '@ionic/react/css/flex-utils.css';
import '@ionic/react/css/display.css';

setupIonicReact();

const AppRoutes: React.FC = () => {
  const { user } = useAuth();

  return (
    <IonReactRouter>
      <IonRouterOutlet>
        <Route exact path="/login">
          {user ? <Redirect to="/dashboard" /> : <Login />}
        </Route>
        
        <Route exact path="/dashboard">
          {user ? <Dashboard /> : <Redirect to="/login" />}
        </Route>
        
        <Route exact path="/">
          <Redirect to={user ? "/dashboard" : "/login"} />
        </Route>
      </IonRouterOutlet>
    </IonReactRouter>
  );
};

const App: React.FC = () => (
  <IonApp>
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  </IonApp>
);

export default App;
```

## 🚀 **DEPLOY EN VERCEL**

### **1. BUILD PARA PRODUCCIÓN**
```bash
# Optimizar para producción
npm run build

# Variables de entorno en Vercel:
REACT_APP_API_URL=https://stockchef-back-production.up.railway.app/api
REACT_APP_ENV=production
```

### **2. CONFIGURACIÓN VERCEL**
```json
// vercel.json
{
  "builds": [
    {
      "src": "package.json",
      "use": "@vercel/static-build",
      "config": {
        "distDir": "dist"
      }
    }
  ],
  "routes": [
    { "handle": "filesystem" },
    { "src": "/.*", "dest": "/index.html" }
  ]
}
```

## 🌟 **RESULTADO FINAL**

```
🌐 Frontend URL: https://stockchef-front.vercel.app
📱 App Móvil: Compilar con Capacitor para iOS/Android
🔗 API Connection: Conectado automáticamente a Railway
🎯 Usuarios: Acceso desde cualquier dispositivo mundial
```

## 📱 **PRÓXIMOS PASOS PARA APP MÓVIL**

```bash
# Compilar para móvil
ionic capacitor add ios
ionic capacitor add android
ionic capacitor build

# Abrir en Xcode/Android Studio
ionic capacitor open ios
ionic capacitor open android
```

**¿Te ayudo a implementar alguna parte específica?** 🚀