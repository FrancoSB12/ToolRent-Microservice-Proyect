import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Header from './Header';
import Sidebar from './Sidebar';

/* This layout have the sidebar and manage his status */
const FullLayout = () => {
  const [isSidebarOpen, setSidebarOpen] = useState(true);

    return (
        <div className="app-container">
            <ToastContainer 
            position="top-center" 
            autoClose={5000} 
            hideProgressBar={false} 
            newestOnTop={false} 
            closeOnClick 
            rtl={false} 
            pauseOnFocusLoss 
            draggable 
            pauseOnHover 
            />
            <Header onToggleSidebar={() => setSidebarOpen(!isSidebarOpen)} />

            <div className="main-body-layout">
                <Sidebar isOpen={isSidebarOpen} />
                <div className="content-area">
                    <Outlet />
                </div>
            </div>
        </div>
    );
};

export default FullLayout;