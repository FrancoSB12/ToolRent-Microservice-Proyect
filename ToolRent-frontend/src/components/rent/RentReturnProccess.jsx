import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useKeycloak } from '@react-keycloak/web';
import rentService from '../../services/rentService';
import '../../styles/Register.css';
import '../../styles/RentReturn.css';

const DAMAGE_OPTIONS = [
    { value: 'NO_DANADA', label: 'En Buen Estado' },
    { value: 'EN_EVALUACION', label: 'Dañada (La herramienta irá a evaluación)' }
];

const RentReturnProcess = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const { keycloak } = useKeycloak();

    const [rent, setRent] = useState(null);
    const [toolConditions, setToolConditions] = useState({});
    const [loading, setLoading] = useState(true);

    //Load the Rent when starting
    useEffect(() => {
        rentService.getById(id)
            .then(response => {
                const data = response.data;
                setRent(data);
                
                // Initialize default conditions
                const initialConditions = {};
                if (data.rentTools) {
                    data.rentTools.forEach(rt => {
                        initialConditions[rt.toolItemId] = 'NO_DANADA';
                    });
                }
                setToolConditions(initialConditions);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error cargando arriendo:", err);
                toast.error("No se pudo cargar la información del arriendo.");
                navigate('/rent/return');
            });
    }, [id, navigate]);

    //Handle change in selectors
    const handleConditionChange = (toolItemId, newCondition) => {
        setToolConditions(prev => ({
            ...prev,
            [toolItemId]: newCondition
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!rent) return;

        const employeeRun = keycloak.tokenParsed?.preferred_username;

        if (!employeeRun) {
            toast.error("No se pudo identificar al empleado. Inicie sesión nuevamente.");
            return;
        }

        //Construct the DTO 'RentReturnRequest'
        const returnRequestPayload = {
            employeeRun: employeeRun,
            returnedTools: rent.rentTools.map(rt => ({
                toolItemId: rt.toolItemId,
                damageLevel: toolConditions[rt.toolItemId]
            }))
        };

        // Send the lightweight payload to the backend
        rentService.returnRent(rent.id, returnRequestPayload)
            .then(() => {
                toast.success(`Devolución del arriendo #${rent.id} registrada con éxito.`);
                navigate('/rent/return');
            })
            .catch(err => {
                console.error(err);
                const msg = err.response?.data?.message || err.response?.data || "Error al procesar la devolución.";
                const displayMsg = typeof msg === 'string' ? msg : "Error interno del servidor.";
                toast.error(displayMsg);
            });
    };

    if (loading) return <div className="full-page-content"><p>Cargando detalles del arriendo...</p></div>;

    return (
        <main className="full-page-content">
            <h2 className="form-title">Procesar Devolución #{rent.id}</h2>

            {/* Rent Information */}
            <div style={{ textAlign: 'center', marginBottom: '2rem', padding: '1rem', background: '#fff', borderRadius: '8px', border: '1px solid #eee' }}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '1rem' }}>
                    <div>
                        <strong>Cliente:</strong><br/>
                        {rent.clientNameSnapshot}
                    </div>
                    <div>
                        <strong>RUT:</strong><br/>
                        {rent.clientRun}
                    </div>
                    <div>
                        <strong>Fecha Préstamo:</strong><br/>
                        {rent.rentDate}
                    </div>
                </div>
            </div>

            <form 
                className="register-form" 
                onSubmit={handleSubmit} 
                style={{ 
                    maxWidth: '900px', 
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '1.5rem'
                }}>

                <h3 style={{ fontSize: '1.1rem', color: '#333', marginBottom: '1rem', borderBottom: '2px solid #eee', paddingBottom: '0.5rem' }}>
                    Evaluar Estado de las Herramientas
                </h3>

                {/* Tool List for Evaluation */}
                <div className="tool-evaluation-list">
                    {rent.rentTools?.map(rt => (
                        <div key={rt.toolItemId} className="tool-evaluation-item">
                            
                            {/* Left block: Information */}
                            <div className="tool-info-block">
                                <h4>
                                    {rt.toolNameSnapshot || "Herramienta Desconocida"}
                                </h4>
                                <span className="serial-number">
                                    Serie: {rt.serialNumberSnapshot || "N/A"}
                                </span>
                            </div>

                            {/* Status selector */}
                            <div className="tool-selector-block">
                                <label htmlFor={`condition-${rt.toolItemId}`}>
                                    Condición de recepción:
                                </label>
                                <select 
                                    id={`condition-${rt.toolItemId}`}
                                    value={toolConditions[rt.toolItemId]} 
                                    onChange={(e) => handleConditionChange(rt.toolItemId, e.target.value)}
                                    /* Mantenemos los estilos dinámicos en línea porque dependen del estado */
                                    className="damage-select-base"
                                    style={{ 
                                        color: toolConditions[rt.toolItemId] !== 'NO_DANADA' ? '#d9534f' : '#333',
                                        fontWeight: toolConditions[rt.toolItemId] !== 'NO_DANADA' ? '600' : 'normal',
                                        borderColor: toolConditions[rt.toolItemId] !== 'NO_DANADA' ? '#d9534f' : '#ccc',
                                        backgroundColor: toolConditions[rt.toolItemId] !== 'NO_DANADA' ? '#fff5f5' : '#f9f9f9'
                                    }}
                                >
                                    {DAMAGE_OPTIONS.map(opt => (
                                        <option key={opt.value} value={opt.value}>{opt.label}</option>
                                    ))}
                                </select>
                            </div>
                        </div>
                    ))}
                </div>

                {/* Action Buttons */}
                <div className="form-actions" style={{ marginTop: '2rem', flexDirection: 'row', gap: '1rem', justifyContent: 'center' }}>
                    
                    <button 
                        type="button" 
                        onClick={() => navigate('/rent/return')}
                        style={{ 
                            background: 'var(--primary-color)',
                            color: '#fff', 
                            border: 'none',
                            padding: '0.8rem 2rem', 
                            borderRadius: '5px', 
                            cursor: 'pointer', 
                            fontSize: '1rem' 
                        }}
                    >
                        Cancelar
                    </button>

                    <button 
                        type="submit" 
                        className="register-submit-btn" 
                        style={{ backgroundColor: '#28a745', width: 'auto', minWidth: '200px' }}
                    >
                        Confirmar Recepción
                    </button>
                    
                </div>
            </form>
        </main>
    );
};

export default RentReturnProcess;