import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import rentService from '../../services/rentService.js'; 
import RentCard from './RentCard.jsx';
import '../../styles/ViewsHome.css';
import { toast } from 'react-toastify';

const ActiveRentsView = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [activeRents, setActiveRents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (initialized) {
      setLoading(true);

      rentService.getByStatus('Activo') 
        .then(response => {
          setActiveRents(response.data);
          setLoading(false);
        })
        .catch(err => {
          console.error("Error al cargar arriendos activos:", err);
          setError(err);
          setLoading(false);
          toast.error("Error al cargar los arriendos activos.");
        });
    }
  }, [initialized]);

  if (loading) return <div className="page-container"><p>Cargando arriendos activos...</p></div>;
  
  if (error) return (
    <div className="page-container">
      <p style={{color: 'red'}}>Error al cargar los arriendos activos.</p>
    </div>
  );

  return (
    <main className="page-container">
      <h2 className="page-title">Arriendos Activos</h2>
            
      <div className="page-actions">
        <button 
          className="action-btn"
          onClick={() => navigate('/rents')}
          style={{ backgroundColor: '#6c757d' }}
        >
        Volver a Todos los Arriendos
        </button>
      </div>
      
      {/* Card grid */}
      <div className="card-grid">
        {activeRents.length > 0 ? (
          activeRents.map(rent => (
            <RentCard key={rent.id} rent={rent} />
          ))
        ) : (
          <p>No hay arriendos con estado "Activo" registrados.</p>
        )}
      </div>
    </main>
  );
};

export default ActiveRentsView;