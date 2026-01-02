import { useNavigate } from "react-router-dom";
import "../../styles/Home.css";

const Home = () => {
  const navigate = useNavigate();
  
  return (
    /* Full page container */
    <main className = "home-dashboard-content">
      <h1>Panel de Control</h1>
      <p>Selecciona una opción para comenzar.</p>
      
      {/* Grid container */}
      <div className="home-nav-buttons">
        <button 
          className="nav-button" 
          onClick={() => navigate('/rent/register')}
        > 
          Realizar un Arriendo
        </button>

        <button 
          className="nav-button" 
          onClick={() => navigate('/rent/return')}
        >
          Realizar una Devolución
        </button>

        <button 
          className="nav-button" 
          onClick={() => navigate('/rents')}
        >
          Gestión de Préstamos
        </button>

        <button 
          className="nav-button" 
          onClick={() => navigate('/employees')}
        >
          Gestión de Empleados
        </button>

        <button 
          className="nav-button" 
          onClick={() => navigate('/clients')}
        >
          Gestión de Clientes 
        </button>

        <button 
          className="nav-button" 
          onClick={() => navigate('/tools')}
        >
          Gestión de Herramientas 
        </button>
      </div>
    </main>
  );
};

export default Home;