function Skeleton({
  width = "100%",
  height = "12px",
  radius = "8px",
  className = "",
}) {
  return (
    <div
      className={`skeleton ${className}`}
      style={{
        width,
        height,
        borderRadius: radius,
      }}
    />
  );
}

export default Skeleton;