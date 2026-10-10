function PageLoader({
  title = "Loading DevSpace...",
  subtitle = "Fetching the latest platform data",
}) {
  return (
    <div className="page-loader">
      <div className="page-loader-spinner" />

      <h3>
        {title}
      </h3>

      <p>
        {subtitle}
      </p>
    </div>
  );
}

export default PageLoader;