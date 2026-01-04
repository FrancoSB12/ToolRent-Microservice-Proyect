import { Outlet } from 'react-router-dom';
import Header from './Header';

/* This layout is for /home, doesn't have sidebar */
const MinimalLayout = () => {
  return (
    <div className="app-container">
      <Header />
      <div className="content-area">
        <Outlet />
      </div>
    </div>
  );
};

export default MinimalLayout;