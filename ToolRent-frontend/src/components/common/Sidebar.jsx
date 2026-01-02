import { NavLink } from "react-router-dom";
import keycloak from '../../services/keycloak.js';
import "../../styles/Sidebar.css";

const Sidebar = ({ isOpen }) => {
    const isAdmin = keycloak.hasRealmRole('Admin');

    return (
        <nav className={isOpen ? 'sidebar open' : 'sidebar'}>
      
            {/* Links main container */}
            <div className="sidebar-main-links">
                <ul className="sidebar-links">
                    <li>
                        <NavLink to="/rent/register">
                        Realizar un Prestamo
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/rent/return">
                        Realizar una Devolución
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/rents">
                        Gestión de Prestamos
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/employees">
                        Gestión de Empleados
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/clients">
                        Gestión de Clientes
                        </NavLink>
                    </li>
                    <li>
                        <NavLink to="/tools">
                        Gestión de Herramientas
                        </NavLink>
                    </li>
                </ul>
            </div>

            {/* Home link container (at the bottom) */}
            <div className="sidebar-footer-links">
                <ul className="sidebar-links">
                    <li>
                        <NavLink to="/home" end>
                        Página Principal
                        </NavLink>
                    </li>
                </ul>
            </div>
        </nav>
    );
};

export default Sidebar;