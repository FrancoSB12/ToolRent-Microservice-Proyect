import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import rentService from '../../services/rentService.js'; 
import RentCard from './RentCard.jsx';
import '../../styles/ViewsHome.css';
import { toast } from 'react-toastify';

const ActiveLoansView = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [activeLoans, setActiveLoans] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (initialized) {
      setLoading(true);

      rentService.getByStatus('Activo') 
        .then(response => {
          setActiveLoans(response.data);
          setLoading(false);
        })
        .catch(err => {
          console.error("Error al cargar préstamos activos:", err);
          setError(err);
          setLoading(false);
          toast.error("Error al cargar los préstamos activos.");
        });
    }
  }, [initialized]);

  if (loading) return <div className="page-container"><p>Cargando préstamos activos...</p></div>;
  
  if (error) return (
    <div className="page-container">
      <p style={{color: 'red'}}>Error al cargar los préstamos activos.</p>
    </div>
  );

  return (
    <main className="page-container">
      <h2 className="page-title">Préstamos Activos (Estado: Activo)</h2>
            
      <div className="page-actions">
        <button 
          className="action-btn"
          onClick={() => navigate('/loans')}
          style={{ backgroundColor: '#6c757d' }}
        >
        Volver a Todos los Préstamos
        </button>
      </div>
      
      {/* Card grid */}
      <div className="card-grid">
        {activeLoans.length > 0 ? (
          activeLoans.map(loan => (
            <RentCard key={loan.id} loan={loan} />
          ))
        ) : (
          <p>No hay préstamos con estado "Activo" registrados.</p>
        )}
      </div>
    </main>
  );
};

export default ActiveLoansView;