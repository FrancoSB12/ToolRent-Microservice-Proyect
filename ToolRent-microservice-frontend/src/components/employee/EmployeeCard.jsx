import '../../styles/EmployeeCard.css';

/* Change the way client status is displayed */
const getEmployeeIsAdminClass = (isAdmin) => {
  if (isAdmin) return 'emp-status-activo';
  if (!isAdmin) return 'emp-status-restringido';
  
  return 'emp-status-default';
};

const EmployeeCard = ({ employee }) => {
  return (
    <div className="employee-card">
        {/* Header with initials */}
        <div className="employee-card-header">
            <div className="employee-avatar">
            {employee.name ? employee.name.charAt(0).toUpperCase() : 'U'}
            </div>
            <div className="employee-info">
            <h3>{employee.name}</h3>
            <h3>{employee.surname}</h3>
            <span className="employee-rut">{employee.rut}</span>
            </div>
        </div>

        {/* Body with details */}
        <div className="employee-card-body">
            <div className="detail-row">
                <strong>Email:</strong>
                <span>{employee.email || 'Sin email'}</span>
            </div>
            <div className="detail-row">
                <strong>Celular:</strong>
                <span>{employee.cellphone || 'Sin celular'}</span>
            </div>
        </div>

        {/* 3. Footer with badge */}
        <div className="employee-card-footer">
            <span className={`emp-role-badge ${employee.isAdmin ? 'role-admin' : 'role-employee'}`}>
                {employee.isAdmin ? 'Administrador' : 'Empleado'}
            </span>
        </div>
    </div>
  );
};

export default EmployeeCard;