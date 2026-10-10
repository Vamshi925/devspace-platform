import Skeleton from "./Skeleton";

function DashboardSkeleton() {
  return (
    <>
      <div className="glass-panel skeleton-hero">
        <div>
          <Skeleton
            width="130px"
            height="10px"
          />

          <div style={{ height: 16 }} />

          <Skeleton
            width="260px"
            height="34px"
          />

          <div style={{ height: 12 }} />

          <Skeleton
            width="430px"
            height="11px"
          />

          <div style={{ height: 8 }} />

          <Skeleton
            width="320px"
            height="11px"
          />
        </div>

        <div className="skeleton-hero-actions">
          <Skeleton
            width="150px"
            height="40px"
            radius="10px"
          />

          <Skeleton
            width="140px"
            height="40px"
            radius="10px"
          />
        </div>
      </div>

      <div className="dashboard-stats-grid">
        {Array.from({ length: 7 }).map(
          (_, index) => (
            <div
              className="dashboard-stat-card"
              key={index}
            >
              <Skeleton
                width="75px"
                height="9px"
              />

              <div style={{ height: 25 }} />

              <Skeleton
                width="38px"
                height="26px"
              />
            </div>
          )
        )}
      </div>

      <div className="dashboard-overview-grid">
        <div className="glass-panel skeleton-panel">
          <Skeleton
            width="150px"
            height="15px"
          />

          <div style={{ height: 10 }} />

          <Skeleton
            width="220px"
            height="9px"
          />

          <div style={{ height: 30 }} />

          <div className="skeleton-health">
            <Skeleton
              width="110px"
              height="110px"
              radius="50%"
            />

            <div className="skeleton-health-grid">
              {Array.from({
                length: 4,
              }).map((_, index) => (
                <Skeleton
                  key={index}
                  height="55px"
                  radius="10px"
                />
              ))}
            </div>
          </div>
        </div>

        <div className="glass-panel skeleton-panel">
          <Skeleton
            width="120px"
            height="15px"
          />

          <div style={{ height: 22 }} />

          {Array.from({
            length: 4,
          }).map((_, index) => (
            <div
              key={index}
              style={{
                marginBottom: 10,
              }}
            >
              <Skeleton
                height="55px"
                radius="10px"
              />
            </div>
          ))}
        </div>
      </div>

      <div className="dashboard-content-grid">
        <div className="glass-panel skeleton-panel">
          <Skeleton
            width="150px"
            height="15px"
          />

          <div style={{ height: 22 }} />

          {Array.from({
            length: 5,
          }).map((_, index) => (
            <div
              key={index}
              style={{
                marginBottom: 12,
              }}
            >
              <Skeleton
                height="48px"
                radius="10px"
              />
            </div>
          ))}
        </div>

        <div className="glass-panel skeleton-panel">
          <Skeleton
            width="120px"
            height="15px"
          />

          <div style={{ height: 22 }} />

          {Array.from({
            length: 4,
          }).map((_, index) => (
            <div
              key={index}
              style={{
                marginBottom: 10,
              }}
            >
              <Skeleton
                height="62px"
                radius="10px"
              />
            </div>
          ))}
        </div>
      </div>
    </>
  );
}

export default DashboardSkeleton;