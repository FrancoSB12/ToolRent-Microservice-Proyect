import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import { toast } from 'react-toastify'; 
import rentService from '../../services/rentService.js'; 
import RentCard from './RentCard.jsx'; 
import '../../styles/ViewsHome.css';

const LoanHome = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [loans, setLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchLoans = useCallback(() => {
    setLoading(true);
    rentService.getAll()
      .then(response => {
        setLoans(response.data);
        setLoading(false);
      })
      .catch(err => {
        console.error("Error al cargar préstamos:", err);
        setError(err);
        setLoading(false);
        toast.error("Error al cargar préstamos. Intente nuevamente.");
      });
  }, []);

  useEffect(() => {
    if (initialized) {
      fetchLoans();
    }
  }, [initialized, fetchLoans]);

  //Function to masive update the validity
  const handleUpdateStatuses = () => {
    rentService.updateLateStatuses() 
      .then(() => {
        toast.success("¡Validez de préstamos actualizada con éxito!");
        //The list is refreshed
        fetchLoans(); 
      })
      .catch(err => {
        console.error("Error al actualizar validez:", err);
        const message = err.response?.data || "Error al actualizar la validez.";
        toast.error(message);
      });
  };

  const handleRegisterClick = () => {
    navigate('/loan/register'); 
  };

  const handleReturnClick = () => {
    navigate('/loan/return'); 
  };

  const handleConfigurationClick = () => {
    navigate('/loan/configuration');
  };

  if (loading) return <div className="page-container"><p>Cargando préstamos...</p></div>;
  
  if (error) return (
    <div className="page-container">
      <p style={{color: 'red'}}>Error al cargar los préstamos.</p>
    </div>
  );

  return (
    <main className="page-container">
      <h2 className="page-title">Gestión de Préstamos</h2>
            
      <div className="page-actions">
        {/* Buttons */}
        <button 
          className="action-btn"
          onClick={handleUpdateStatuses}
          style={{ marginRight: '10px', backgroundColor: '#df590bff' }}
        >
          Actualizar Validez de los Préstamos
        </button>
        
        <button 
          className="action-btn"
          onClick={handleRegisterClick}
        >
        Registrar Nuevo Préstamo
        </button>

        <button 
          className="action-btn"
          onClick={handleReturnClick}
        >
          Devolver un Préstamo
        </button>

        <button 
          className="action-btn"
          onClick={handleConfigurationClick}
        >
          Configurar Multa por Atraso
        </button>

        <button 
          className="action-btn"
          onClick={() => navigate('/loans/active')} 
        >
          Ver Préstamos Activos
        </button>
      </div>
      
      <div className="card-grid">
        {loans.length > 0 ? (
          loans.map(loan => (
            <RentCard key={loan.id} loan={loan} /> 
          ))
        ) : (
          <p>No hay préstamos registrados.</p>
        )}
      </div>
    </main>
  );
};

export default LoanHome;