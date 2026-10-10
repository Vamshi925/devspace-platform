import Skeleton from "./Skeleton";

function NotificationsSkeleton() {
  return (
    <>
      <div className="page-header">
        <div>
          <Skeleton
            width="170px"
            height="28px"
          />

          <div style={{ height: 10 }} />

          <Skeleton
            width="280px"
            height="10px"
          />
        </div>
      </div>

      <div className="notification-filter-bar">
        <Skeleton
          width="70px"
          height="34px"
          radius="9px"
        />

        <Skeleton
          width="90px"
          height="34px"
          radius="9px"
        />

        <Skeleton
          width="80px"
          height="34px"
          radius="9px"
        />
      </div>

      <div className="notifications-list">
        {Array.from({
          length: 6,
        }).map((_, index) => (
          <div
            className="notification-card"
            key={index}
          >
            <Skeleton
              width="42px"
              height="42px"
              radius="11px"
            />

            <div
              style={{
                flex: 1,
              }}
            >
              <Skeleton
                width="170px"
                height="11px"
              />

              <div style={{ height: 8 }} />

              <Skeleton
                width="80%"
                height="9px"
              />

              <div style={{ height: 6 }} />

              <Skeleton
                width="60%"
                height="9px"
              />

              <div style={{ height: 10 }} />

              <Skeleton
                width="110px"
                height="8px"
              />
            </div>
          </div>
        ))}
      </div>
    </>
  );
}

export default NotificationsSkeleton;