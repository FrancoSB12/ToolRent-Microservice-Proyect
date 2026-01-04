import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import toolItemService from '../../services/toolItemService.js';
import '../../styles/Register.css';

//Options to disable tool item
const DISABLE_OPTIONS = [
  { value: 'DESUSO', label: 'Baja Administrativa (Pérdida/Obsolescencia)/A Dada De Baja' },
  { value: 'LEVEMENTE_DANADA', label: 'Daño Leve/A En Reparación' },
  { value: 'DANADA', label: 'Dañada/A En Reparación' },
  { value: 'GRAVEMENTE_DANADA', label: 'Gravemente Dañada/A En Reparación' },
  { value: 'IRREPARABLE', label: 'Irreparable/A Dada De Baja' }
];

const ToolItemDisable = () => {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        serialNumber: '',
        damageLevel: ''
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        const disableData = {
        damageLevel: formData.damageLevel
        };

        toolItemService.disableItem(formData.serialNumber, disableData)
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
            <h2 className="form-title">Deshabilitar o Enviar a Reparación</h2>
            
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

                {/* Damage select */}
                <div className="form-group" style={{ gridColumn: '1 / -1' }}>
                    <select 
                        name="damageLevel" 
                        value={formData.damageLevel} 
                        onChange={handleChange}
                        required
                        style={{ color: formData.damageLevel === '' ? '#888' : '#000' }}
                    >
                        <option value="" disabled hidden>Seleccione Motivo/Nivel de daño</option>
                        
                        {DISABLE_OPTIONS.map(option => (
                        <option key={option.value} value={option.value}>
                            {option.label}
                        </option>
                        ))}
                    </select>
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

export default ToolItemDisable;