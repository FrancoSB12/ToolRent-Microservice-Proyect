import { useState } from 'react';
import { toast } from 'react-toastify';
import { Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper } from '@mui/material';
import kardexService from '../../services/kardexService'; 
import '../../styles/ViewsHome.css'; 

const KardexDateRangeReport = () => {
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [kardexMovements, setKardexMovements] = useState([]);
    const [loading, setLoading] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!startDate || !endDate) {
            toast.error("Debes seleccionar una fecha de inicio y una fecha de fin.");
            return;
        }

        //Simple date comparison
        if (new Date(startDate) > new Date(endDate)) {
            toast.error("La fecha de inicio no puede ser posterior a la fecha de fin.");
            return;
        }
        
        setLoading(true);

        kardexService.getKardexByDateRange(startDate, endDate)
            .then(res => {
                setKardexMovements(res.data || []);
                setLoading(false);
                if (res.data.length === 0) {
                    toast.info("No se encontraron movimientos en el rango de fechas seleccionado.");
                }
            })
            .catch(err => {
                console.error("Error cargando Kardex por rango de fechas:", err);
                const msg = err.response?.data || "Error al cargar los datos. Verifique el rango.";
                toast.error(msg);
                setKardexMovements([]);
                setLoading(false);
            });
    };

    return (
        <main className="full-page-content">
            <h2 className="form-title">Reporte de Kardex por Rango de Fechas</h2>
            
            <form onSubmit={handleSubmit} style={{ maxWidth: '600px', margin: '0 auto 2rem', padding: '1rem', display: 'flex', gap: '1rem', alignItems: 'flex-end', border: '1px solid #ccc', borderRadius: '8px', backgroundColor: 'white' }}>

                <div className="form-group">
                    <label htmlFor="startDate">Fecha Inicio:</label>
                    <input 
                        type="date" 
                        id="startDate"
                        value={startDate} 
                        onChange={(e) => setStartDate(e.target.value)}
                        style={{ height: '50px' }}
                        required
                    />
                </div>

                <div className="form-group">
                    <label htmlFor="endDate">Fecha Fin:</label>
                    <input 
                        type="date" 
                        id="endDate"
                        value={endDate} 
                        onChange={(e) => setEndDate(e.target.value)}
                        style={{ height: '50px' }}
                        required 
                    />
                </div>

                <button 
                    type="submit" 
                    className="action-btn"
                    style={{ height: '50px' }}
                    disabled={loading}
                >
                    {loading ? 'Buscando...' : 'Buscar'}
                </button>
            </form>

            <div style={{ margin: '0 auto', maxWidth: '1000px' }}>
                <TableContainer component={Paper} style={{ maxHeight: 600 }}>
                    <Table stickyHeader aria-label="kardex date report table">
                        <TableHead>
                            <TableRow style={{ backgroundColor: '#f5f5f5' }}>
                                <TableCell>ID</TableCell>
                                <TableCell>Fecha</TableCell>
                                <TableCell>Tipo de Herramienta</TableCell>
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
                                        <TableCell>{movement.toolTypeName || 'N/A'}</TableCell> 
                                        <TableCell>{movement.operationType}</TableCell>
                                        <TableCell align="right">{movement.stockInvolved}</TableCell>
                                    </TableRow>
                                ))
                            ) : (
                                <TableRow>
                                    <TableCell colSpan={5} align="center">
                                        {loading ? 'Cargando...' : 'Ingrese un rango de fechas para generar el reporte.'}
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>
            </div>
        </main>
    );
};

export default KardexDateRangeReport;