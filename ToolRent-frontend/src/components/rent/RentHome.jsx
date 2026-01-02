import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { toast } from 'react-toastify'; 
import rentService from '../../services/rentService.js'; 
import RentCard from './RentCard.jsx'; 
import '../../styles/ViewsHome.css';

const RentHome = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [rents, setRents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchRents = useCallback(() => {
    setLoading(true);
    rentService.getAll()
      .then(response => {
        setRents(response.data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Error al cargar arriendos:", err);
        setError(err);
        setLoading(false);
        toast.error("Error al cargar arriendos. Intente nuevamente.");
      });
  }, []);

  useEffect(() => {
    if (initialized) {
      fetchRents();
    }
  }, [initialized, fetchRents]);

  //Function to masive update the validity
  const handleUpdateStatuses = () => {
    rentService.updateLateStatuses() 
      .then(() => {
        toast.success("¡Validez de arriendos actualizada con éxito!");
        //The list is refreshed
        fetchRents(); 
      })
      .catch(err => {
        console.error("Error al actualizar validez:", err);
        const message = err.response?.data || "Error al actualizar la validez.";
        toast.error(message);
      });
  };

  const handleRegisterClick = () => {
    navigate('/rent/register'); 
  };

  const handleReturnClick = () => {
    navigate('/rent/return'); 
  };

  const handleConfigurationClick = () => {
    navigate('/rent/configuration');
  };

  if (loading) return <div className="page-container"><p>Cargando arriendos...</p></div>;
  
  if (error) return (
    <div className="page-container">
      <p style={{color: 'red'}}>Error al cargar los arriendos.</p>
    </div>
  );

  return (
    <main className="page-container">
      <h2 className="page-title">Gestión de Arriendos</h2>
            
      <div className="page-actions">
        {/* Buttons */}
        <button 
          className="action-btn"
          onClick={handleUpdateStatuses}
          style={{ marginRight: '10px', backgroundColor: '#0b80dfff' }}
        >
          Actualizar Validez de los Arriendos
        </button>
        
        <button 
          className="action-btn"
          onClick={handleRegisterClick}
        >
        Registrar Nuevo Arriendo
        </button>

        <button 
          className="action-btn"
          onClick={handleReturnClick}
        >
          Devolver un Arriendo
        </button>

        <button 
          className="action-btn"
          onClick={handleConfigurationClick}
        >
          Configurar Multa por Atraso
        </button>

        <button 
          className="action-btn"
          onClick={() => navigate('/rents/active')} 
        >
          Ver Arriendos Activos
        </button>
      </div>
      
      <div className="card-grid">
        {rents.length > 0 ? (
          rents.map(rent => (
            console.log(rent),
            <RentCard key={rent.id} rent={rent} /> 
          ))
        ) : (
          <p>No hay arriendos registrados.</p>
        )}
      </div>
    </main>
  );
};

export default RentHome;