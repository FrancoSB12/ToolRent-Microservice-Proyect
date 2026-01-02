import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import { 
    Table, TableBody, TableCell, TableContainer, 
    TableHead, TableRow, Paper, Chip 
} from '@mui/material';
import toolTypeService from '../../services/toolTypeService'; 
import kardexService from '../../services/kardexService'; 
import '../../styles/ViewsHome.css'; 

const ToolTypeKardexView = () => {
    const [toolTypes, setToolTypes] = useState([]);
    const [selectedToolType, setSelectedToolType] = useState(null);
    const [kardexMovements, setKardexMovements] = useState([]);
    const [loadingTypes, setLoadingTypes] = useState(true);

    //Load tool types
    useEffect(() => {
        toolTypeService.getAllTypes()
            .then(res => {
                setToolTypes(res.data);
                setLoadingTypes(false);
            })
            .catch(err => {
                console.error("Error cargando tipos de herramienta:", err);
                toast.error("Error al cargar la lista de herramientas.");
                setLoadingTypes(false);
            });
    }, []);

    //Load tool kardex movements when a tool type is selected
    useEffect(() => {
        if (selectedToolType) {
            const toolName = selectedToolType.name;
            
            setKardexMovements([]);

            kardexService.getKardexByToolName(toolName)
                .then(res => {
                    setKardexMovements(res.data || []); 
                })
                .catch(err => {
                    console.error("Error cargando Kardex:", err);
                    //If the error is 404, it means there are no movements yet, so we don't show an error.
                    if (err.response && err.response.status === 404) {
                        setKardexMovements([]);
                    } else {
                        toast.error("Error al cargar los movimientos.");
                    }
                });
        } else {
            setKardexMovements([]);
        }
    }, [selectedToolType]);

    //Helper function to format date
    const formatDate = (dateString) => {
        if(!dateString) return "-";
        const date = new Date(dateString);
        return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
    };

    //Function to give visual color to the operation type
    const getOperationColor = (operation) => {
        const op = operation ? operation.toUpperCase() : '';
        if (op.includes('ALTA') || op.includes('INGRESO') || op.includes('DEVOLUCION')) return 'success';
        if (op.includes('BAJA') || op.includes('SALIDA') || op.includes('PRESTAMO')) return 'error';
        return 'default';
    };

    if (loadingTypes) {
        return <main className="full-page-content"><p>Cargando lista de herramientas...</p></main>;
    }

    return (
        <main className="full-page-content">
            <h2 className="form-title">Historial de Movimientos de Kardex</h2>
            
            <div style={{ maxWidth: '600px', margin: '0 auto 2rem', padding: '1rem', border: '1px solid #ccc', borderRadius: '8px', backgroundColor: 'white' }}>
                <label htmlFor="toolTypeSelect" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Seleccionar Tipo de Herramienta:
                </label>
                <Autocomplete
                    id="toolTypeSelect"
                    options={toolTypes}
                    getOptionLabel={(option) => `${option.name} (${option.model})`}
                    onChange={(event, newValue) => {
                        setSelectedToolType(newValue);
                    }}
                    value={selectedToolType}
                    renderInput={(params) => <TextField {...params} label="Buscar Tipo de Herramienta" variant="outlined" size="small" />}
                />
            </div>

            {selectedToolType && (
                <div style={{ margin: '0 auto', maxWidth: '1000px' }}>
                    <h3 style={{ marginBottom: '1rem', textAlign: 'center', color: '#333' }}>
                        Movimientos para: <span style={{color: '#1976d2'}}>{selectedToolType.name}</span>
                    </h3>

                    <TableContainer component={Paper} elevation={3} style={{ maxHeight: 600 }}>
                        <Table stickyHeader aria-label="kardex table">
                            <TableHead>
                                <TableRow>
                                    <TableCell style={{ backgroundColor: '#f5f5f5', fontWeight: 'bold' }}>ID</TableCell>
                                    <TableCell style={{ backgroundColor: '#f5f5f5', fontWeight: 'bold' }}>Fecha</TableCell>
                                    <TableCell style={{ backgroundColor: '#f5f5f5', fontWeight: 'bold' }}>Tipo de Operación</TableCell>
                                    <TableCell align="right" style={{ backgroundColor: '#f5f5f5', fontWeight: 'bold' }}>Stock Inv.</TableCell>
                                    <TableCell style={{ backgroundColor: '#f5f5f5', fontWeight: 'bold' }}>Responsable</TableCell>
                                    <TableCell style={{ backgroundColor: '#f5f5f5', fontWeight: 'bold' }}>RUN</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {kardexMovements.length > 0 ? (
                                    kardexMovements.map((movement) => (
                                        <TableRow key={movement.id} hover>
                                            <TableCell>#{movement.id}</TableCell>
                                            <TableCell>{formatDate(movement.date)}</TableCell>
                                            <TableCell>
                                                <Chip 
                                                    label={movement.operationType} 
                                                    color={getOperationColor(movement.operationType)} 
                                                    size="small" 
                                                    variant="outlined"
                                                />
                                            </TableCell>
                                            <TableCell align="right" style={{ fontWeight: 'bold' }}>
                                                {movement.stockInvolved > 0 ? `+${movement.stockInvolved}` : movement.stockInvolved}
                                            </TableCell>
                                            <TableCell>{movement.employeeNameSnapshot || 'N/A'}</TableCell>
                                            <TableCell>{movement.employeeRun || 'N/A'}</TableCell>
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={5} align="center" style={{ padding: '2rem', color: '#666' }}>
                                            No hay movimientos registrados para <strong>{selectedToolType.name}</strong>.
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                </div>
            )}
        </main>
    );
};

export default ToolTypeKardexView;