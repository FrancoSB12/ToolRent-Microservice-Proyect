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
  const handleToolRentalFeeConfigClick = () => { navigate('/tools/rental-fee-config'); };
  const handleToolReplacementValueConfigClick = () => { navigate('/tools/replacement-value-config'); };
  const handleToolKardexClick = () => { navigate('/tools/kardex-view'); };
  const handleToolDateRangeReportClick = () => { navigate('/tools/kardex-date-range-report'); };
  const handleToolMostRentedRankingClick = () => { navigate('/tools/most-rented-ranking'); };

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
          onClick={handleToolRentalFeeConfigClick}
        >
          Configurar Tarifa de Arriendo
        </button>

        <button 
          className="action-btn"
          onClick={handleToolReplacementValueConfigClick}
        >
          Configurar Valor de Reemplazo
        </button>

        <button 
          className="action-btn"
          onClick={handleToolKardexClick}
        >
          Ver movimientos de Herramientas
        </button>

        <button 
          className="action-btn"
          onClick={handleToolDateRangeReportClick}
        >
          Ver movimientos entre fechas
        </button>

        <button 
          className="action-btn"
          onClick={handleToolMostRentedRankingClick}
        >
          Ver Ranking de Herramientas más Arriendas
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