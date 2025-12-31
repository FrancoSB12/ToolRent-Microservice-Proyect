import { useKeycloak } from '@react-keycloak/web';
import '../../styles/PublicHome.css';
import '../../styles/Header.css';

/* This view is displayed when someone isn't logged in */
const PublicHome = () => {
  const { keycloak } = useKeycloak();

    const handleLogin = () => {
        keycloak.login({redirectUri: window.location.origin + '/home'});
    };

    return (
    <div className="public-home-container">
      <header className="header-container">
        <div className="header-left-group">
          <h1 className="header-title"> Bienvenido a Tool Rent </h1>
        </div>
        <button onClick={handleLogin} className="header-action-button"> Iniciar  sesión </button>
      </header>

      <main className="public-main-content">
        <h2>Gestión de Herramientas Simplificada</h2>
        <p>Una descripción de tu increíble aplicación...</p>
      </main>

    </div>
  );
};

export default PublicHome;