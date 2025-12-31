import React from "react";
import { useNavigate } from "react-router-dom";
import { useKeycloak } from "@react-keycloak/web";
import '../../styles/Header.css';

const Header = ({ onToggleSidebar }) => {
  const { keycloak } = useKeycloak();
  const navigate = useNavigate();
  const handleGoHome = () => navigate('/home');
  
  const getUserName = () => {
  if (!keycloak.authenticated) return '';
  return keycloak.tokenParsed?.name || keycloak.tokenParsed?.preferred_username;
};
  return (
    <header className="header-container">
      <div className="header-left-group">
        {/* ¡CONDITIONAL RENDER! */}
        {/* The button ☰ only appears if onToggleSidebar exists */}
        {onToggleSidebar && (
          <button onClick={onToggleSidebar} className="header-menu-button"> ☰ </button>
        )}

        <h1 className="header-title" onClick={handleGoHome}>
          {keycloak.authenticated ? `Bienvenido, ${getUserName()}` : 'Bienvenido a Tool Rent'}
        </h1>
      </div>
      <div>
        {keycloak.authenticated && (
          <button 
            onClick={() => keycloak.logout({ redirectUri: window.location.origin })}
            className="header-action-button" 
          >
            Cerrar sesión
          </button>
        )}
      </div>
    </header>
  );
};

export default Header;