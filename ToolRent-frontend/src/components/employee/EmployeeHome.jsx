import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useKeycloak } from '@react-keycloak/web';
import employeeService from '../../services/employeeService.js';
import EmployeeCard from './EmployeeCard.jsx';
import '../../styles/ViewsHome.css';
import keycloak from '../../services/keycloak.js';

const EmployeeHome = () => {
  const navigate = useNavigate();
  const { initialized } = useKeycloak();

  const [employees, setEmployee] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const handleRegisterClick = () => {
    navigate('/employees/register');
  };

  useEffect(() => {
    if (initialized) {
      employeeService.getAll()
        .then(response => {
          setEmployee(response.data);
          setLoading(false);
        })
        .catch(err => {
          console.error("Error al cargar empleados:", err);
          setError(err);
          setLoading(false);
        });
    }
  }, [initialized]);

  if (loading) return <div className="page-container"><p>Cargando empleados...</p></div>;
  
  if (error) return (
    <div className="page-container">
      <p style={{color: 'red'}}>Error al cargar los empleados.</p>
    </div>
  );

  return (
    <main className="page-container">
      <h2 className="page-title">Gestión de Empleados</h2>
            
      <div className="page-actions">
        <button 
          className="action-btn"
          onClick={handleRegisterClick}
        >
        Registrar Empleado
        </button>
      </div>
      
      {/* Card grid */}
      <div className="card-grid">
        {employees.length > 0 ? (
          employees.map(emp => (
            <EmployeeCard key={emp.id} employee={emp} />
          ))
        ) : (
          <p>No hay empleados registrados.</p>
        )}
      </div>
    </main>
  );
};

export default EmployeeHome;