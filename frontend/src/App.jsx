import { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import { Activity, Play, RefreshCw, ServerCrash, Clock, DollarSign, Database, Terminal } from 'lucide-react';
import './index.css';

const NODES = [
  { id: 1, port: 8081 },
  { id: 2, port: 8082 },
  { id: 3, port: 8083 }
];

function App() {
  const [nodeStates, setNodeStates] = useState({
    1: { connected: false, state: null, clock: 0, logs: [] },
    2: { connected: false, state: null, clock: 0, logs: [] },
    3: { connected: false, state: null, clock: 0, logs: [] }
  });
  const [leaderId, setLeaderId] = useState(null);
  const [isFiring, setIsFiring] = useState(false);
  const stompClients = useRef({});
  const logsEndRefs = useRef({});

  useEffect(() => {
    // Initialize connections for all nodes
    NODES.forEach(node => {
      fetchInitialState(node);
      setupWebSocket(node);
    });

    return () => {
      Object.values(stompClients.current).forEach(client => {
        if (client && client.active) {
          client.deactivate();
        }
      });
    };
  }, []);

  const updateNodeData = (nodeId, data) => {
    setNodeStates(prev => ({
      ...prev,
      [nodeId]: { ...prev[nodeId], ...data }
    }));
  };

  const addLog = (nodeId, message) => {
    setNodeStates(prev => {
      const currentLogs = prev[nodeId].logs;
      const newLogs = [...currentLogs, `[${new Date().toLocaleTimeString()}] ${message}`];
      if (newLogs.length > 50) newLogs.shift(); // Keep last 50
      return {
        ...prev,
        [nodeId]: { ...prev[nodeId], logs: newLogs }
      };
    });
    
    // Auto-scroll
    setTimeout(() => {
      if (logsEndRefs.current[nodeId]) {
        logsEndRefs.current[nodeId].scrollIntoView({ behavior: 'smooth' });
      }
    }, 100);
  };

  const fetchInitialState = async (node) => {
    try {
      const res = await axios.get(`http://localhost:${node.port}/api/stocks/state`);
      updateNodeData(node.id, { connected: true, state: res.data });
    } catch (err) {
      console.error(`Failed to fetch state for Node ${node.id}`, err);
      updateNodeData(node.id, { connected: false });
    }
  };

  const setupWebSocket = (node) => {
    const client = new Client({
      brokerURL: `ws://localhost:${node.port}/ws`,
      reconnectDelay: 5000,
      onConnect: () => {
        updateNodeData(node.id, { connected: true });
        
        client.subscribe('/topic/lamport', (message) => {
          const body = JSON.parse(message.body);
          if (body.nodeId === node.id) {
            updateNodeData(node.id, { clock: body.clock });
          }
        });

        client.subscribe('/topic/state', (message) => {
          const body = JSON.parse(message.body);
          if (body.nodeId === node.id) {
            updateNodeData(node.id, { state: body });
          }
        });

        client.subscribe('/topic/leader', (message) => {
          const body = JSON.parse(message.body);
          setLeaderId(body.leaderNodeId);
        });

        client.subscribe('/topic/logs', (message) => {
          const body = JSON.parse(message.body);
          if (body.nodeId === node.id) {
            addLog(node.id, body.message);
          }
        });
      },
      onDisconnect: () => {
        updateNodeData(node.id, { connected: false });
      }
    });
    
    client.activate();
    stompClients.current[node.id] = client;
  };

  const handleBuy = async (nodeId, quantity = 1) => {
    const node = NODES.find(n => n.id === nodeId);
    try {
      await axios.post(`http://localhost:${node.port}/api/stocks/buy`, {
        symbol: 'STR',
        quantity
      });
    } catch (err) {
      console.error(`Buy failed on Node ${nodeId}`, err);
      addLog(nodeId, `Buy Error: ${err.response?.data || err.message}`);
    }
  };

  const fireConcurrentBuys = async () => {
    setIsFiring(true);
    // Send 5 concurrent buy requests across different nodes
    const promises = [];
    for (let i = 0; i < 5; i++) {
      const randomNode = NODES[Math.floor(Math.random() * NODES.length)];
      // add small random delay to simulate network jitter but keep them very close
      promises.push(
        new Promise(resolve => setTimeout(resolve, Math.random() * 50))
          .then(() => handleBuy(randomNode.id, 1))
      );
    }
    
    await Promise.allSettled(promises);
    setIsFiring(false);
  };

  const syntaxHighlight = (json) => {
    if (!json) return '';
    const str = JSON.stringify(json, null, 2);
    return str.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
        let cls = 'json-number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'json-key';
            } else {
                cls = 'json-string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'json-string';
        } else if (/null/.test(match)) {
            cls = 'json-string';
        }
        return `<span class="${cls}">${match}</span>`;
    });
  };

  return (
    <div className="app-container">
      <header className="header">
        <h1>Distributed Stock Market</h1>
        
        <div className="global-controls">
          <button 
            className="btn btn-primary" 
            onClick={fireConcurrentBuys}
            disabled={isFiring}
          >
            {isFiring ? <RefreshCw className="animate-spin" size={18} /> : <Play size={18} />}
            Concurrent Buy (x5)
          </button>
        </div>
      </header>

      <div className="nodes-grid">
        {NODES.map(node => {
          const data = nodeStates[node.id];
          const isLeader = leaderId === node.id;
          
          return (
            <div key={node.id} className={`node-card ${isLeader ? 'is-leader' : ''}`}>
              <div className="node-header">
                <div className="node-title-group">
                  <h2>
                    <span className={`node-status ${data.connected ? '' : 'offline'}`}></span>
                    Node {node.id}
                  </h2>
                  {isLeader && <span className="leader-badge">Coordinator</span>}
                </div>
                <div className="lamport-badge">
                  <Clock size={16} /> L: {data.clock}
                </div>
              </div>

              <div className="node-stats">
                <div className="stat-box">
                  <div className="stat-label">Balance</div>
                  <div className="stat-value" style={{ display: 'flex', alignItems: 'center' }}>
                    <DollarSign size={20} />
                    {data.state?.accountBalance?.toFixed(2) || '0.00'}
                  </div>
                </div>
                <div className="stat-box">
                  <div className="stat-label">STR Shares</div>
                  <div className="stat-value">
                    {data.state?.marketStocks?.[0]?.availableShares || 0}
                  </div>
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-secondary)' }}>
                  <Database size={16} />
                  <span style={{ fontSize: '0.9rem' }}>node{node.id}_db.json</span>
                </div>
                <pre 
                  className="json-viewer"
                  dangerouslySetInnerHTML={{ __html: syntaxHighlight(data.state) }}
                />
              </div>

              <div className="actions-panel">
                <button 
                  className="btn" 
                  onClick={() => handleBuy(node.id, 1)}
                  disabled={!data.connected}
                >
                  Buy 1 STR
                </button>
              </div>

              <div className="logs-panel">
                <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem', color: '#fff' }}>
                  <Terminal size={14} /> Activity Log
                </div>
                {data.logs.map((log, i) => (
                  <div key={i} className="log-entry">{log}</div>
                ))}
                <div ref={el => logsEndRefs.current[node.id] = el} />
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

export default App;
