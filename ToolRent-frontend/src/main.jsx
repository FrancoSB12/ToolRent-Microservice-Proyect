import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { ReactKeycloakProvider } from '@react-keycloak/web';

import keycloak from './services/keycloak.js';
import App from './App.jsx'
import './index.css'

const onKeycloakEvent = (event, error) => {
  if(event === 'onAuthSuccess') {
    console.log('Keycloak: Autenticación exitosa');
  }
};

const root = createRoot(document.getElementById('root'));

root.render(
  <StrictMode>
    <ReactKeycloakProvider
      authClient={keycloak}
      onEvent={onKeycloakEvent}
      initOptions={{ onLoad: 'check-sso' }}
    >
      <BrowserRouter>
        <App />
      </BrowserRouter>
    </ReactKeycloakProvider>
  </StrictMode>
);
