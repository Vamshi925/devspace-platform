import {
  CheckCircle2,
  Circle,
  LoaderCircle,
  TriangleAlert,
} from "lucide-react";

const stages = [
  "STARTING",
  "NAMESPACE_CREATED",
  "RESOURCES_CONFIGURED",
  "DEPLOYMENT_CREATED",
  "SERVICE_CREATED",
  "INGRESS_CREATED",
  "WAITING_FOR_READINESS",
  "READY",
];

const stageLabels = {
  STARTING: "Starting",
  NAMESPACE_CREATED: "Namespace Created",
  RESOURCES_CONFIGURED: "Resources Configured",
  DEPLOYMENT_CREATED: "Deployment Created",
  SERVICE_CREATED: "Service Created",
  INGRESS_CREATED: "Ingress Created",
  WAITING_FOR_READINESS: "Waiting for Readiness",
  READY: "Environment Ready",
};

function ProvisioningProgress({
  stage,
  status,
}) {
  const currentIndex =
    stages.indexOf(stage);

  const failed =
    status === "FAILED";

  const finished =
    status === "READY";

  return (
    <div className="glass-panel provisioning-progress">
      <div className="provisioning-progress-header">
        <div>
          <h3>
            Provisioning Progress
          </h3>

          <p>
            {failed
              ? "Provisioning failed before the environment became ready."
              : finished
                ? "Your environment has been provisioned successfully."
                : "DevSpace is preparing your development environment."}
          </p>
        </div>

        {!failed &&
          !finished && (
            <div className="provisioning-live">
              <LoaderCircle
                size={14}
                className="provisioning-spinner"
              />

              Live
            </div>
          )}

        {finished && (
          <div className="provisioning-ready-label">
            <CheckCircle2
              size={14}
            />

            Ready
          </div>
        )}

        {failed && (
          <div className="provisioning-failed-label">
            <TriangleAlert
              size={14}
            />

            Failed
          </div>
        )}
      </div>

      <div className="provisioning-steps">
        {stages.map(
          (
            stageName,
            index
          ) => {
            const complete =
              finished ||
              index < currentIndex;

            const active =
              !failed &&
              !finished &&
              index === currentIndex;

            const waiting =
              index >
              currentIndex;

            return (
              <div
                className={`provisioning-step ${
                  complete
                    ? "complete"
                    : ""
                } ${
                  active
                    ? "active"
                    : ""
                } ${
                  waiting
                    ? "waiting"
                    : ""
                }`}
                key={stageName}
              >
                <div className="provisioning-step-marker">
                  {complete ? (
                    <CheckCircle2
                      size={18}
                    />
                  ) : active ? (
                    <LoaderCircle
                      size={18}
                      className="provisioning-spinner"
                    />
                  ) : (
                    <Circle
                      size={18}
                    />
                  )}

                  {index <
                    stages.length -
                      1 && (
                    <div className="provisioning-step-line" />
                  )}
                </div>

                <div className="provisioning-step-content">
                  <strong>
                    {
                      stageLabels[
                        stageName
                      ]
                    }
                  </strong>

                  <span>
                    {complete
                      ? "Completed"
                      : active
                        ? "In progress"
                        : "Pending"}
                  </span>
                </div>
              </div>
            );
          }
        )}
      </div>
    </div>
  );
}

export default ProvisioningProgress;