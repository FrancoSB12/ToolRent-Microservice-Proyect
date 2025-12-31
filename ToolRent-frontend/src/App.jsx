import './App.css'
import { useState } from 'react'
import { Route, Routes, Navigate} from 'react-router-dom'
import { useKeycloak } from '@react-keycloak/web';

import Home from './components/home/Home'
import PublicHome from './components/home/PublicHome';
import FullLayout from './components/common/FullLayout';
import MinimalLayout from './components/common/MinimalLayout';
import ToolHome from './components/tool/ToolHome';
import ToolKardexView from './components/tool/ToolKardexView.jsx';
import ToolKardexDateRangeReport from './components/tool/ToolKardexDateRangeReport.jsx';
import ToolItemHome from './components/tool/ToolItemHome.jsx';
import ToolTypeRegister from './components/tool/ToolTypeRegister';
import ToolTypeRentalFeeConfig from './components/tool/ToolTypeRentalFeeConfig.jsx';
import ToolTypeReplacementValueConfig from './components/tool/ToolTypeReplacementValueConfig.jsx';
import ToolItemRegister from './components/tool/ToolItemRegister.jsx';
import ToolItemEnable from './components/tool/ToolItemEnable.jsx';
import ToolItemDisable from './components/tool/ToolItemDisable.jsx';
import ToolItemEvaluateDamage from './components/tool/ToolItemEvaluateDamage.jsx';
import ClientHome from './components/client/ClientHome.jsx';
import ClientRegister from './components/client/ClientRegister.jsx';
import EmployeeHome from './components/employee/EmployeeHome.jsx';
import EmployeeRegister from './components/employee/EmployeeRegister.jsx';
import RentHome from './components/rent/RentHome.jsx';
import RentRegister from './components/rent/RentRegister.jsx';
import RentReturn from './components/rent/RentReturn.jsx';
import RentReturnProccess from './components/rent/RentReturnProccess.jsx';
import RentLateFeeConfig from './components/rent/RentLateFeeConfig.jsx';
import RentActiveView from './components/rent/RentActiveView.jsx';

function App() {
  const { keycloak, initialized } = useKeycloak();

  if(!initialized) {
    return <div>Cargando...</div>;
  }

  /* PUBLIC RENDER */
  /* If the user isn't logged in, only the public path is displayed */
  if(!keycloak.authenticated) {
    return (
      <Routes>
        <Route path = "/" element = {<PublicHome />} />
        <Route path = "*" element = {<Navigate to = "/" replace />} />
      </Routes>
    );
  }

  /* Roles logic */
  const roles = keycloak.tokenParsed?.realm_access?.roles || [];
  const PrivateRoute = ({ element, rolesAllowed }) => {
    const hasPermission = rolesAllowed.some(role => roles.includes(role));
    if(hasPermission) {
      /* If has permission, render the component */
      return element;
    }

    /* If doesn't have permission, display an access denied page */
    return (
      <main className='page-centered-content'>
        <h1>Acceso Denegado</h1>
        <p>No tienes permiso para acceder a esta página.</p>
      </main>
    );
  };

  /* PRIVATE RENDER */
  /* If the user is logged in, all the layout of the app is displayed */
  return (
    <Routes>
      
      {/* Minimal layout (without sidebar) for /home */}
      <Route element={<MinimalLayout />}>
        <Route 
          path="/home" 
          element={
            <PrivateRoute 
              element={<Home />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          }
        />
      </Route>

      {/* Complete layout (with sidebar) for the rest of the app */}
      <Route element={<FullLayout />}>
        <Route 
          path="/employees" 
          element={
            <PrivateRoute 
              element={<EmployeeHome />} 
              rolesAllowed={['Admin']} 
            />
          } 
        />

        <Route 
          path="/employees/register" 
          element={
            <PrivateRoute 
              element={<EmployeeRegister />} 
              rolesAllowed={['Admin']} 
            />
          } 
        />

        <Route 
          path="/clients" 
          element={
            <PrivateRoute 
              element={<ClientHome />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />

        <Route 
          path="/clients/register" 
          element={
            <PrivateRoute 
              element={<ClientRegister />} 
              rolesAllowed={['Admin']} 
            />
          } 
        />

        <Route 
          path="/tools" 
          element={
            <PrivateRoute 
              element={<ToolHome />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />

        <Route 
          path="/tools/tool-items" 
          element={
            <PrivateRoute 
              element={<ToolItemHome />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />

        <Route 
          path="/tools/register-tool-type" 
          element={
            <PrivateRoute 
              element={<ToolTypeRegister />} 
              rolesAllowed={['Admin']} 
            />
          } 
        />

        <Route 
          path="/tools/tool-items/register-tool-item" 
          element={
            <PrivateRoute 
              element={<ToolItemRegister />} 
              rolesAllowed={['Admin']} 
            />
          } 
        />

        <Route 
          path="/tools/tool-items/enable" 
          element={
            <PrivateRoute 
              element={<ToolItemEnable />} 
              rolesAllowed={['Admin']}
            />
          } 
        />
        
        <Route 
          path="/tools/tool-items/disable" 
          element={
            <PrivateRoute 
              element={<ToolItemDisable />} 
              rolesAllowed={['Admin']}
            />
          } 
        />

        <Route 
          path="/tools/tool-items/evaluation" 
          element={
            <PrivateRoute 
              element={<ToolItemEvaluateDamage />} 
              rolesAllowed={['Admin']}
            />
          } 
        />

        <Route 
          path="/tools/rental-fee-config" 
          element={
            <PrivateRoute 
              element={<ToolTypeRentalFeeConfig />} 
              rolesAllowed={['Admin']}
            />
          } 
        />

        <Route 
          path="/tools/replacement-value-config" 
          element={
            <PrivateRoute 
              element={<ToolTypeReplacementValueConfig />} 
              rolesAllowed={['Admin']}
            />
          } 
        />

        <Route 
          path="/tools/kardex-view" 
          element={
            <PrivateRoute 
              element={<ToolKardexView />} 
              rolesAllowed={['Admin', 'Employee']}
            />
          } 
        />

        <Route 
          path="/tools/kardex-date-range-report" 
          element={
            <PrivateRoute 
              element={<ToolKardexDateRangeReport />} 
              rolesAllowed={['Admin', 'Employee']}
            />
          } 
        />


        <Route 
          path="/loans" 
          element={
            <PrivateRoute 
              element={<RentHome />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />

        <Route 
          path="/loan/register" 
          element={
            <PrivateRoute 
              element={<RentRegister />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />

        <Route 
          path="/loan/return" 
          element={
            <PrivateRoute 
              element={<RentReturn />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />

        <Route 
          path="/loan/return/:id" 
          element={
            <PrivateRoute 
              element={<RentReturnProccess />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />

        <Route 
          path="/loan/configuration" 
          element={
            <PrivateRoute 
              element={<RentLateFeeConfig />} 
              rolesAllowed={['Admin']} 
            />
          } 
        />
        <Route 
          path="/loans/active" 
          element={
            <PrivateRoute 
              element={<RentActiveView />} 
              rolesAllowed={['Admin', 'Employee']} 
            />
          } 
        />
      </Route>

      {/* Redirectionts and 404 */}
      <Route path="/" element={<Navigate to="/home" replace />} />
      <Route path="*" element={
        <main className='page-centered-content'> 
          <h1> 404: Página no encontrada </h1>
        </main>
      }/>
    </Routes>
  );
}

export default App