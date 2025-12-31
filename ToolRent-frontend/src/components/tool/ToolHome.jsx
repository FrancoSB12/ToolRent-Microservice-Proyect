import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';

import toolTypeService from '../../services/toolTypeService.js';
import ToolTypeCard from './ToolTypeCard.jsx';
import '../../styles/ViewsHome.css';

const ToolsHome = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [toolTypes, setToolTypes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const handleRegisterClickToolType = () => { navigate('/tools/register-tool-type'); };
  const handleViewToolItemClick = () => { navigate('/tools/tool-items'); };

  useEffect(() => {
    /* Fetch tools data if logged in to keycloak */
    if (initialized) {
      toolTypeService.getAllTypes()
        .then(response => {
          setToolTypes(response.data);
          setLoading(false);
        })
        .catch(err => {
          console.error('Error al obtener las herramientas:', err);
          setError(err);
          setLoading(false);
        });
    }
  }, [initialized]);

  if (loading) {
    return (
      <main className="page-container">
        <h2>Gestion de Herramientas</h2>
        <p>Cargando herramientas...</p>
      </main>
    );
  }
  
  if (error) {
    return (
      <main className="page-container">
        <h2>Gestion de Herramientas</h2>
        <p >Error al cargar las herramientas. ¿Tienes los permisos correctos?</p>
      </main>
    );
  }

  return (
    <main className="page-container">
      <h2 className="page-title">Gestión de Herramientas</h2>

      <div className="page-actions">
        <button 
          className="action-btn"
          onClick={handleRegisterClickToolType}
        >
        Registrar Tipo de Herramienta
        </button>

        <button 
          className="action-btn"
          onClick={handleViewToolItemClick}
        >
        Ver Unidades de Herramienta
        </button>

        <button 
          className="action-btn"
          onClick={() => navigate('/tools/rental-fee-config')} 
        >
          Configurar Tarifa de Arriendo
        </button>

        <button 
          className="action-btn"
          onClick={() => navigate('/tools/replacement-value-config')} 
        >
          Configurar Valor de Reemplazo
        </button>

        <button 
          className="action-btn"
          onClick={() => navigate('/tools/kardex-view')} 
        >
          Ver movimientos de Herramientas
        </button>

        <button 
          className="action-btn"
          onClick={() => navigate('/tools/kardex-date-range-report')} 
        >
          Ver movimientos entre fechas
        </button>
      </div>
      
      <div className="card-grid">
        {toolTypes.length > 0 ? (
          toolTypes.map(toolType => (
            <ToolTypeCard key={toolType.id} toolType={toolType} />
          ))
        ) : (
          <p>No hay herramientas registradas.</p>
        )}
      </div>
    </main>
  );
};

export default ToolsHome;