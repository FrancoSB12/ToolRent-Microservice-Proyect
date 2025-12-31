import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import Autocomplete from '@mui/material/Autocomplete';
import TextField from '@mui/material/TextField';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import toolTypeService from '../../services/toolTypeService'; 
import kardexService from '../../services/kardexService'; 
import '../../styles/ViewsHome.css'; 

const ToolTypeKardexView = () => {
    const [toolTypes, setToolTypes] = useState([]);
    const [selectedToolType, setSelectedToolType] = useState(null);
    const [kardexMovements, setKardexMovements] = useState([]);
    const [loading, setLoading] = useState(true);

    //Load all tool types
    useEffect(() => {
        toolTypeService.getAllTypes()
            .then(res => {
                setToolTypes(res.data);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error cargando tipos de herramienta:", err);
                toast.error("Error al cargar la lista de herramientas.");
                setLoading(false);
            });
    }, []);

    //Load kardex movements when a tool type is selected
    useEffect(() => {
        if (selectedToolType) {
            const toolName = selectedToolType.name; 
            
            kardexService.getkardexByToolName(toolName)
                .then(res => {
                    setKardexMovements(res.data || []); 
                })
                .catch(err => {
                    console.error("Error cargando Kardex:", err);
                    toast.error("Error al cargar los movimientos de Kardex.");
                    setKardexMovements([]);
                });
        } else {
            setKardexMovements([]);
        }
    }, [selectedToolType]);


    if (loading) {
        return <main className="full-page-content"><p>Cargando lista de herramientas...</p></main>;
    }

    return (
        <main className="full-page-content">
            <h2 className="form-title">Historial de Movimientos de Kardex</h2>
            
            <div style={{ maxWidth: '600px', margin: '0 auto 2rem', padding: '1rem', border: '1px solid #ccc', borderRadius: '8px' }}>
                <label htmlFor="toolTypeSelect" style={{ display: 'block', marginBottom: '0.5rem', fontWeight: 'bold' }}>
                    Seleccionar Tipo de Herramienta:
                </label>
                <Autocomplete
                    id="toolTypeSelect"
                    options={toolTypes}
                    getOptionLabel={(option) => `${option.name} (${option.model}) - Stock Total: ${option.totalStock}`}
                    onChange={(event, newValue) => {
                        setSelectedToolType(newValue);
                    }}
                    value={selectedToolType}
                    renderInput={(params) => <TextField {...params} label="Buscar Tipo de Herramienta" />}
                />
            </div>

            {selectedToolType && (
                <div style={{ margin: '0 auto', maxWidth: '1000px' }}>
                    <h3 style={{ marginBottom: '1rem', textAlign: 'center' }}>
                        Movimientos para **{selectedToolType.name} ({selectedToolType.model})**
                    </h3>

                    <TableContainer component={Paper} style={{ maxHeight: 600 }}>
                        <Table stickyHeader aria-label="kardex table">
                            <TableHead>
                                <TableRow style={{ backgroundColor: '#f5f5f5' }}>
                                    <TableCell>ID</TableCell>
                                    <TableCell>Fecha</TableCell>
                                    <TableCell>Tipo de Operación</TableCell>
                                    <TableCell align="right">Stock Involucrado</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {kardexMovements.length > 0 ? (
                                    kardexMovements.map((movement) => (
                                        <TableRow key={movement.id} hover>
                                            <TableCell>{movement.id}</TableCell>
                                            <TableCell>{movement.date}</TableCell>
                                            <TableCell>{movement.operationType}</TableCell>
                                            <TableCell align="right">{movement.stockInvolved}</TableCell>
                                        </TableRow>
                                    ))
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={4} align="center">
                                            No hay movimientos de Kardex registrados para este tipo de herramienta.
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