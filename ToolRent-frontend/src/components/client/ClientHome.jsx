import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';

import keycloak from '../../services/keycloak.js';
import clientService from '../../services/clientService.js';
import ClientCard from './ClientCard.jsx';
import '../../styles/ViewsHome.css';

const ClientHome = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const handleRegisterClick = () => {
    navigate('/clients/register');
  };

  useEffect(() => {
    if (initialized) {
      clientService.getAll()
        .then(response => {
          setClients(response.data);
          setLoading(false);
        })
        .catch(err => {
          console.error("Error al cargar clientes:", err);
          setError(err);
          setLoading(false);
        });
    }
  }, [initialized]);

  if (loading) return <div className="page-container"><p>Cargando clientes...</p></div>;
  
  if (error) return (
    <div className="page-container">
      <p style={{color: 'red'}}>Error al cargar los clientes.</p>
    </div>
  );

  return (
    <main className="page-container">
      <h2 className="page-title">Gestión de Clientes</h2>
            
      <div className="page-actions">
        <button 
          className="action-btn"
          onClick={handleRegisterClick}
        >
        Registrar Cliente
        </button>
      </div>
      
      {/* Card grid */}
      <div className="card-grid">
        {clients.length > 0 ? (
          clients.map(emp => (
            <ClientCard key={emp.id} client={emp} />
          ))
        ) : (
          <p>No hay clientes registrados.</p>
        )}
      </div>
    </main>
  );
};

export default ClientHome;