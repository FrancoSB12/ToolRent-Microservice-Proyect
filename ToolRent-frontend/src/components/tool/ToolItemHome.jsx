import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';

import toolItemService from '../../services/toolItemService.js';
import ToolItemCard from './ToolItemCard.jsx';
import '../../styles/ViewsHome.css';

const ToolItemHome = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [toolItems, setToolItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const handleRegisterClickToolItem = () => { navigate('/tools/tool-items/register-tool-item'); };
  const handleEnableToolItemClick = () => { navigate('/tools/tool-items/enable'); };
  const handleDisableToolItemClick = () => { navigate('/tools/tool-items/disable'); };
  const handleEvaluateDamageClick = () => { navigate('/tools/tool-items/evaluation'); };

  useEffect(() => {
    /* Fetch tools data if logged in to keycloak */
    if (initialized) {
      toolItemService.getAllItems()
        .then(response => {
          setToolItems(response.data);
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
        <h2>Gestion de Unidades de Herramientas</h2>
        <p>Cargando unidades de herramientas...</p>
      </main>
    );
  }
  
  if (error) {
    return (
      <main className="page-container">
        <h2>Gestion de Unidades de Herramientas</h2>
        <p >Error al cargar las unidades de herramientas. ¿Tienes los permisos correctos?</p>
      </main>
    );
  }

  return (
    <main className="page-container">
      <h2 className="page-title">Gestión de Unidades de Herramientas</h2>

      <div className="page-actions">
        <button 
          className="action-btn"
          onClick={handleRegisterClickToolItem}
        >
        Registrar Unidad de Herramienta
        </button>

        <button 
          className="action-btn"
          onClick={handleEnableToolItemClick}
        >
        Habilitar Unidad de Herramienta
        </button>

        <button 
          className="action-btn"
          onClick={handleEvaluateDamageClick}
        >
        Evaluar Daño de Unidad de Herramienta
        </button>

        <button 
          className="action-btn"
          onClick={handleDisableToolItemClick}
          style={{ backgroundColor: 'var(--error-color)' }}
        >
        Deshabilitar Unidad de Herramienta
        </button>
      </div>
      
      <div className="card-grid">
        {toolItems.length > 0 ? (
          toolItems.map(toolItem => (
            <ToolItemCard key={toolItem.id} toolItem={toolItem} />
          ))
        ) : (
          <p>No hay unidades de herramientas registradas.</p>
        )}
      </div>
    </main>
  );
};

export default ToolItemHome;