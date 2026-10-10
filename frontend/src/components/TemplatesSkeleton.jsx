import Skeleton from "./Skeleton";

function TemplatesSkeleton() {
  return (
    <>
      <div className="page-header">
        <div>
          <Skeleton
            width="180px"
            height="28px"
          />

          <div style={{ height: 10 }} />

          <Skeleton
            width="320px"
            height="10px"
          />
        </div>
      </div>

      <div className="templates-grid">
        {Array.from({
          length: 6,
        }).map((_, index) => (
          <div
            className="template-card"
            key={index}
          >
            <div className="template-card-top">
              <Skeleton
                width="42px"
                height="42px"
                radius="11px"
              />

              <Skeleton
                width="65px"
                height="22px"
                radius="999px"
              />
            </div>

            <div style={{ height: 18 }} />

            <Skeleton
              width="150px"
              height="16px"
            />

            <div style={{ height: 10 }} />

            <Skeleton
              width="90%"
              height="9px"
            />

            <div style={{ height: 8 }} />

            <Skeleton
              width="70%"
              height="9px"
            />

            <div style={{ height: 20 }} />

            {Array.from({
              length: 4,
            }).map((_, itemIndex) => (
              <div
                key={itemIndex}
                style={{
                  marginBottom: 10,
                }}
              >
                <Skeleton
                  height="42px"
                  radius="9px"
                />
              </div>
            ))}

            <div style={{ height: 14 }} />

            <Skeleton
              height="34px"
              radius="9px"
            />
          </div>
        ))}
      </div>
    </>
  );
}

export default TemplatesSkeleton;