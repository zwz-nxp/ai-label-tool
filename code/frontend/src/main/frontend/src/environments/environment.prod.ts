export const environment = {
  production: true,
  server: "",
  maxFileSize: 10485760, // 10MB in bytes
  supportedFormats: ["image/jpeg", "image/png", "image/bmp"],
  maxConcurrentUploads: 5,
  defaultConfidenceThreshold: 0.5,
  chartColors: {
    trainLoss: "#FF6384",
    valLoss: "#36A2EB",
    mAP: "#4BC0C0",
  },
};
