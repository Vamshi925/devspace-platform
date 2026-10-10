import { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  Activity,
  Bell,
  CircleCheckBig,
  CircleX,
  Clock3,
  Eye,
  Layers3,
  LoaderCircle,
  Plus,
  Rocket,
  Server,
  ShieldCheck,
  Sparkles,
  Trash2,
} from "lucide-react";
import { getDashboardSummary } from "../api/dashboardApi";
import { getMyEnvironments } from "../api/environmentApi";
import { getMyNotifications } from "../api/notificationApi";
import { useAuth } from "../context/AuthContext";
import DashboardSkeleton from "../components/DashboardSkeleton";
import { NOTIFICATIONS_UPDATED } from "../utils/appEvents";

function Dashboard() {
  const { user }=useAuth();
  const navigate=useNavigate();
  const [summary,setSummary]=useState(null);
  const [environments,setEnvironments]=useState([]);
  const [notifications,setNotifications]=useState([]);
  const [loading,setLoading]=useState(true);
  const [error,setError]=useState("");

  const loadDashboard=useCallback(async(showLoader=true)=>{
    try {
      if(showLoader) setLoading(true);

      const [summaryData,environmentData,notificationData]=await Promise.all([
        getDashboardSummary(),
        getMyEnvironments(),
        getMyNotifications(),
      ]);

      setSummary(summaryData);
      setEnvironments(environmentData||[]);
      setNotifications(notificationData||[]);
      setError("");
    } catch(error) {
      console.error("Dashboard loading failed:",error.response?.data||error);
      if(showLoader) setError(error.response?.data?.message||"Unable to load dashboard");
    } finally {
      if(showLoader) setLoading(false);
    }
  },[]);

  useEffect(()=>{
    loadDashboard(true);

    const refresh=()=>loadDashboard(false);
    const interval=setInterval(()=>loadDashboard(false),15000);

    window.addEventListener(NOTIFICATIONS_UPDATED,refresh);

    return()=>{
      clearInterval(interval);
      window.removeEventListener(NOTIFICATIONS_UPDATED,refresh);
    };
  },[loadDashboard]);

  const recentEnvironments=useMemo(
    ()=>[...environments]
      .sort((a,b)=>new Date(b.createdAt)-new Date(a.createdAt))
      .slice(0,5),
    [environments]
  );

  const recentNotifications=useMemo(
    ()=>[...notifications]
      .sort((a,b)=>new Date(b.createdAt)-new Date(a.createdAt))
      .slice(0,4),
    [notifications]
  );

  const activeCount=(summary?.ready||0)+(summary?.provisioning||0);

  const healthPercentage=
    summary?.total>0
      ?Math.round(((summary.ready||0)/summary.total)*100)
      :0;

  const stats=summary?[
    {label:"Total",value:summary.total,icon:<Server size={20}/>,className:"purple"},
    {label:"Ready",value:summary.ready,icon:<CircleCheckBig size={20}/>,className:"green"},
    {label:"Provisioning",value:summary.provisioning,icon:<LoaderCircle size={20}/>,className:"cyan"},
    {label:"Failed",value:summary.failed,icon:<CircleX size={20}/>,className:"red"},
    {label:"Expired",value:summary.expired,icon:<Clock3 size={20}/>,className:"orange"},
    {label:"Deleted",value:summary.deleted,icon:<Trash2 size={20}/>,className:"gray"},
    {label:"Unread",value:summary.unreadNotifications,icon:<Bell size={20}/>,className:"pink"},
  ]:[];

  const getStatusClass=(status)=>{
    switch(status){
      case "READY":
        return "status-ready";
      case "PROVISIONING":
      case "REQUESTED":
        return "status-provisioning";
      case "FAILED":
        return "status-failed";
      case "EXPIRED":
        return "status-expired";
      case "DELETING":
      case "DELETED":
      default:
        return "status-deleted";
    }
  };

  const getNotificationIcon=(type)=>{
    switch(type){
      case "ENVIRONMENT_READY":
        return <CircleCheckBig size={16}/>;
      case "ENVIRONMENT_FAILED":
        return <CircleX size={16}/>;
      case "ENVIRONMENT_EXPIRING":
      case "ENVIRONMENT_EXPIRED":
        return <Clock3 size={16}/>;
      default:
        return <Bell size={16}/>;
    }
  };

  if(loading) return <DashboardSkeleton/>;

  if(error){
    return (
      <div className="glass-panel form-card">
        <h2>Something went wrong</h2>
        <p>{error}</p>
      </div>
    );
  }

  return (
    <>
      <section className="dashboard-hero">
        <div className="dashboard-hero-content">
          <div className="dashboard-eyebrow">
            <Sparkles size={14}/>
            DevSpace Workspace
          </div>

          <h1>
            Welcome back<span>.</span>
          </h1>

          <p>Provision, monitor and manage your developer environments from one place.</p>

          <div className="dashboard-user-meta">
            <span>{user?.email}</span>
            <span className={`dashboard-role-badge ${user?.role==="ROLE_ADMIN"?"admin":"developer"}`}>
              {user?.role==="ROLE_ADMIN"?"Administrator":"Developer"}
            </span>
          </div>
        </div>

        <div className="dashboard-hero-actions">
          <button className="btn btn-primary" onClick={()=>navigate("/environments/create")}>
            <Rocket size={17}/>
            Launch Environment
          </button>
          <button className="btn btn-ghost" onClick={()=>navigate("/environments")}>
            <Activity size={17}/>
            View Environments
          </button>
        </div>

        <div className="dashboard-hero-glow"/>
      </section>

      <div className="dashboard-stats-grid">
        {stats.map((stat)=>(
          <div
            key={stat.label}
            className={`dashboard-stat-card dashboard-stat-${stat.className}`}
          >
            <div className="dashboard-stat-header">
              <span>{stat.label}</span>
              <div className="dashboard-stat-icon">{stat.icon}</div>
            </div>
            <strong>{stat.value}</strong>
          </div>
        ))}
      </div>

      <div className="dashboard-overview-grid">
        <div className="glass-panel dashboard-health-card">
          <div className="dashboard-section-header">
            <div>
              <h2>Environment Health</h2>
              <p>Current developer environment activity</p>
            </div>
            <Activity size={19}/>
          </div>

          <div className="dashboard-health-content">
            <div className="dashboard-health-circle">
              <div>
                <strong>{healthPercentage}%</strong>
                <span>Ready</span>
              </div>
            </div>

            <div className="dashboard-health-details">
              <div>
                <span>Active Environments</span>
                <strong>{activeCount}</strong>
              </div>
              <div>
                <span>Ready</span>
                <strong>{summary.ready}</strong>
              </div>
              <div>
                <span>Provisioning</span>
                <strong>{summary.provisioning}</strong>
              </div>
              <div>
                <span>Failed</span>
                <strong>{summary.failed}</strong>
              </div>
            </div>
          </div>

          <div className="dashboard-health-bar">
            <div style={{width:`${healthPercentage}%`}}/>
          </div>
        </div>

        <div className="glass-panel dashboard-actions-panel">
          <div className="dashboard-section-header">
            <div>
              <h2>Quick Actions</h2>
              <p>Common DevSpace operations</p>
            </div>
          </div>

          <div className="dashboard-action-list">
            <button onClick={()=>navigate("/environments/create")}>
              <div className="dashboard-action-icon"><Plus size={18}/></div>
              <div>
                <strong>New Environment</strong>
                <span>Launch from an approved template</span>
              </div>
            </button>

            <button onClick={()=>navigate("/templates")}>
              <div className="dashboard-action-icon"><Layers3 size={18}/></div>
              <div>
                <strong>Browse Templates</strong>
                <span>Explore available platform templates</span>
              </div>
            </button>

            <button onClick={()=>navigate("/environments")}>
              <div className="dashboard-action-icon"><Server size={18}/></div>
              <div>
                <strong>My Environments</strong>
                <span>Inspect your active environments</span>
              </div>
            </button>

            {user?.role==="ROLE_ADMIN"&&(
              <button onClick={()=>navigate("/admin/environments")}>
                <div className="dashboard-action-icon dashboard-action-admin">
                  <ShieldCheck size={18}/>
                </div>
                <div>
                  <strong>Platform Administration</strong>
                  <span>Manage system-wide resources</span>
                </div>
              </button>
            )}
          </div>
        </div>
      </div>

      <div className="dashboard-content-grid">
        <div className="glass-panel dashboard-recent-panel">
          <div className="dashboard-section-header">
            <div>
              <h2>Recent Environments</h2>
              <p>Your latest developer environments</p>
            </div>

            <button className="dashboard-section-link" onClick={()=>navigate("/environments")}>
              View all
            </button>
          </div>

          {recentEnvironments.length===0?(
            <div className="dashboard-empty">
              <Server size={27}/>
              <strong>No environments yet</strong>
              <span>Launch your first developer environment.</span>
              <button className="btn btn-primary" onClick={()=>navigate("/environments/create")}>
                <Plus size={15}/>
                Create Environment
              </button>
            </div>
          ):(
            <div className="dashboard-recent-list">
              {recentEnvironments.map((environment)=>(
                <div className="dashboard-environment-row" key={environment.environmentId}>
                  <div className="dashboard-env-icon">
                    <Server size={17}/>
                  </div>

                  <div className="dashboard-env-main">
                    <strong>{environment.applicationName}</strong>
                    <span>{environment.environmentCode}</span>
                  </div>

                  <span className={`status-badge ${getStatusClass(environment.status)}`}>
                    {environment.status}
                  </span>

                  <span className="dashboard-env-type">{environment.environmentType}</span>

                  <button
                    className="dashboard-view-button"
                    aria-label={`View ${environment.applicationName}`}
                    onClick={()=>navigate(`/environments/${environment.environmentId}`)}
                  >
                    <Eye size={15}/>
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="glass-panel dashboard-notifications-panel">
          <div className="dashboard-section-header">
            <div>
              <h2>Recent Activity</h2>
              <p>Latest platform notifications</p>
            </div>

            {summary.unreadNotifications>0&&(
              <span className="dashboard-unread-count">{summary.unreadNotifications}</span>
            )}
          </div>

          {recentNotifications.length===0?(
            <div className="dashboard-empty dashboard-empty-small">
              <Bell size={24}/>
              <span>No notifications yet.</span>
            </div>
          ):(
            <div className="dashboard-notification-list">
              {recentNotifications.map((notification)=>(
                <div
                  key={notification.notificationId}
                  className={`dashboard-notification-item ${!notification.read?"unread":""}`}
                >
                  <div className="dashboard-notification-icon">
                    {getNotificationIcon(notification.type)}
                  </div>

                  <div>
                    <strong>{notification.type?.replaceAll("_"," ")}</strong>
                    <p>{notification.message}</p>
                    <span>
                      {notification.createdAt
                        ?new Date(notification.createdAt).toLocaleString()
                        :""}
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}

          <button
            className="dashboard-notification-footer"
            onClick={()=>navigate("/notifications")}
          >
            View all notifications
          </button>
        </div>
      </div>
    </>
  );
}

export default Dashboard;