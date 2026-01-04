import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import toolItemService from '../../services/toolItemService.js';
import '../../styles/Register.css';

const ToolItemEnable = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        serialNumber: ''
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        toolItemService.enableItem(formData.serialNumber)
        .then(response => {
            const tool = response.data;
            const toolName = tool.toolType ? tool.toolType.name : 'Herramienta';
            toast.success(`${toolName} (Serie: ${tool.serialNumber}) actualizada correctamente.`);
            navigate('/tools/tool-items');
        })
        .catch(err => {
            console.error(err);
            if (err.response && err.response.status === 404) {
                toast.error("No se encontró ninguna herramienta con ese Número de Serie.");
            } else {
                const msg = err.response?.data || "Error al procesar la solicitud.";
                toast.error(msg);
            }
        });
    };

    return (
        <main className="full-page-content">
            <h2 className="form-title">Habilitar Unidad de Herramienta</h2>
            
            <form className="register-form" onSubmit={handleSubmit} style={{ maxWidth: '600px' }}>
                
                {/* Serial number input */}
                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <input 
                        type="text" 
                        name="serialNumber" 
                        value={formData.serialNumber} 
                        onChange={handleChange} 
                        placeholder="Ingrese Número de Serie" 
                        required 
                    />
                </div>

                {/* Action Button */}
                <div className="form-actions">
                    <button 
                        type="submit" 
                        className="register-submit-btn"
                    >
                        Confirmar Estado
                    </button>
                </div>
            </form>
        </main>
    );
};

export default ToolItemEnable;