import { useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import { 
    Paper, Button, TextField, Box, LinearProgress, Typography,
    Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
    Tooltip
} from '@mui/material';
import BarChartIcon from '@mui/icons-material/BarChart';
import SearchIcon from '@mui/icons-material/Search';
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff';
import EmojiEventsIcon from '@mui/icons-material/EmojiEvents'; 
import reportService from '../../services/reportService'; 
import '../../styles/ViewsHome.css'; 

const ToolRankingView = () => {
    const [rankingData, setRankingData] = useState([]);
    const [loading, setLoading] = useState(true);
    
    const [fromDate, setFromDate] = useState('');
    const [toDate, setToDate] = useState('');

    const fetchRanking = () => {
        setLoading(true);
        reportService.getRankingReport(fromDate, toDate)
            .then(res => {
                setRankingData(res.data || []);
                setLoading(false);
            })
            .catch(err => {
                console.error("Error cargando ranking:", err);
                toast.error("Error al cargar el ranking de herramientas.");
                setLoading(false);
            });
    };

    useEffect(() => {
        fetchRanking();
    }, []);

    const handleClearFilters = () => {
        setFromDate('');
        setToDate('');
        setTimeout(() => {
            reportService.getRankingReport(null, null)
                .then(res => setRankingData(res.data || []))
                .catch(() => {});
        }, 100);
    };

    const maxCount = rankingData.length > 0 ? Math.max(...rankingData.map(item => item.totalRents)) : 1;

    const getRankIcon = (index) => {
        if (index === 0) return <EmojiEventsIcon style={{ color: '#FFD700' }} />; 
        if (index === 1) return <EmojiEventsIcon style={{ color: '#C0C0C0' }} />; 
        if (index === 2) return <EmojiEventsIcon style={{ color: '#CD7F32' }} />; 
        return <span style={{ fontWeight: 'bold', color: '#666', paddingLeft: '4px' }}>{index + 1}</span>;
    };

    return (
        <main className="full-page-content">
            <h2 className="form-title" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '10px' }}>
                <BarChartIcon fontSize="large" color="primary" /> 
                Ranking de Herramientas Más Usadas
            </h2>
            
            <div style={{ margin: '0 auto', maxWidth: '1000px' }}>
                
                <Paper elevation={1} style={{ padding: '1.5rem', marginBottom: '2rem', backgroundColor: '#f9f9f9' }}>
                    <Box display="flex" gap={2} alignItems="center" flexWrap="wrap" justifyContent="center">
                        <TextField
                            label="Desde"
                            type="date"
                            value={fromDate}
                            onChange={(e) => setFromDate(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            size="small"
                            style={{ backgroundColor: 'white' }}
                        />
                        <TextField
                            label="Hasta"
                            type="date"
                            value={toDate}
                            onChange={(e) => setToDate(e.target.value)}
                            InputLabelProps={{ shrink: true }}
                            size="small"
                            style={{ backgroundColor: 'white' }}
                        />
                        <Button 
                            variant="contained" 
                            color="primary" 
                            startIcon={<SearchIcon />}
                            onClick={fetchRanking}
                        >
                            Filtrar
                        </Button>
                        {(fromDate || toDate) && (
                            <Button 
                                variant="outlined" 
                                color="secondary" 
                                startIcon={<FilterAltOffIcon />}
                                onClick={handleClearFilters}
                            >
                                Limpiar
                            </Button>
                        )}
                    </Box>
                </Paper>

                {loading ? (
                    <p style={{textAlign: 'center'}}>Calculando estadísticas...</p>
                ) : (
                    <TableContainer component={Paper} elevation={3}>
                        <Table aria-label="ranking table">
                            <TableHead>
                                <TableRow style={{ backgroundColor: '#e3f2fd' }}>
                                    <TableCell align="center" width="10%"><strong>Posición</strong></TableCell>
                                    <TableCell width="40%"><strong>Herramienta</strong></TableCell>
                                    <TableCell width="35%"><strong>Popularidad Visual</strong></TableCell>
                                    <TableCell align="right" width="15%"><strong>Préstamos</strong></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {rankingData.length > 0 ? (
                                    rankingData.map((row, index) => {
                                        const percentage = (row.totalRents / maxCount) * 100;
                                        
                                        return (
                                            <TableRow key={row.toolTypeId} hover>
                                                <TableCell align="center" style={{ fontSize: '1.2rem' }}>
                                                    {getRankIcon(index)}
                                                </TableCell>
                                                
                                                <TableCell style={{ fontWeight: index < 3 ? 'bold' : 'normal' }}>
                                                    {row.toolName}
                                                </TableCell>
                                                
                                                <TableCell>
                                                    <Box display="flex" alignItems="center">
                                                        <Box width="100%" mr={1}>
                                                            <Tooltip title={`${percentage.toFixed(1)}% del máximo`}>
                                                                <LinearProgress 
                                                                    variant="determinate" 
                                                                    value={percentage} 
                                                                    style={{ 
                                                                        height: 10, 
                                                                        borderRadius: 5,
                                                                        backgroundColor: '#e0e0e0',
                                                                    }}
                                                                    sx={{
                                                                        '& .MuiLinearProgress-bar': {
                                                                            backgroundColor: index === 0 ? '#FFD700' : index === 1 ? '#9e9e9e' : index === 2 ? '#d84315' : '#1976d2'
                                                                        }
                                                                    }}
                                                                />
                                                            </Tooltip>
                                                        </Box>
                                                    </Box>
                                                </TableCell>

                                                <TableCell align="right">
                                                    <Typography variant="body1" style={{ fontWeight: 'bold', color: '#333' }}>
                                                        {row.totalRents}
                                                    </Typography>
                                                </TableCell>
                                            </TableRow>
                                        );
                                    })
                                ) : (
                                    <TableRow>
                                        <TableCell colSpan={4} align="center" style={{ padding: '3rem', color: '#666' }}>
                                            No hay datos de préstamos para el rango de fechas seleccionado.
                                        </TableCell>
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                )}
            </div>
        </main>
    );
};

export default ToolRankingView;