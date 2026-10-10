import Skeleton from "./Skeleton";

function EnvironmentTableSkeleton() {
  return (
    <>
      <div className="page-header">
        <div>
          <Skeleton
            width="190px"
            height="28px"
          />

          <div style={{ height: 10 }} />

          <Skeleton
            width="310px"
            height="10px"
          />
        </div>

        <Skeleton
          width="150px"
          height="40px"
          radius="10px"
        />
      </div>

      <div className="environment-toolbar">
        <Skeleton
          width="320px"
          height="40px"
          radius="10px"
        />

        <Skeleton
          width="150px"
          height="40px"
          radius="10px"
        />
      </div>

      <div className="table-container">
        <table className="dev-table">
          <thead>
            <tr>
              <th>Application</th>
              <th>Status</th>
              <th>Stage</th>
              <th>Type</th>
              <th>Expires</th>
              <th>Environment URL</th>
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {Array.from({
              length: 6,
            }).map((_, index) => (
              <tr key={index}>
                <td>
                  <Skeleton
                    width="120px"
                    height="10px"
                  />

                  <div style={{ height: 7 }} />

                  <Skeleton
                    width="95px"
                    height="8px"
                  />
                </td>

                <td>
                  <Skeleton
                    width="68px"
                    height="22px"
                    radius="999px"
                  />
                </td>

                <td>
                  <Skeleton
                    width="90px"
                    height="9px"
                  />
                </td>

                <td>
                  <Skeleton
                    width="80px"
                    height="9px"
                  />
                </td>

                <td>
                  <Skeleton
                    width="120px"
                    height="9px"
                  />
                </td>

                <td>
                  <Skeleton
                    width="95px"
                    height="9px"
                  />
                </td>

                <td>
                  <Skeleton
                    width="80px"
                    height="30px"
                    radius="8px"
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

export default EnvironmentTableSkeleton;