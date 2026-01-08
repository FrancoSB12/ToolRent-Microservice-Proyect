import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ReactKeycloakProvider } from '@react-keycloak/web';
import keycloak from './services/keycloak.js';
import App from './App.jsx'
import './index.css'

const onKeycloakEvent = (event, error) => {
  console.log('Keycloak Event:', event, error);
};

const root = createRoot(document.getElementById('root'));

// SIN StrictMode
root.render(
    <ReactKeycloakProvider
      authClient={keycloak}
      onEvent={onKeycloakEvent}
      initOptions={{ 
        onLoad: 'check-sso',
        checkLoginIframe: false
      }}
    >
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ReactKeycloakProvider>
);
