module.exports = {
  publicPath: './',
  outputDir: 'dist',
  assetsDir: 'static',
  productionSourceMap: false,
  devServer: {
    host: '0.0.0.0',
    port: 8081,
    open: false,
    proxy: {
      '/kingdee': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true
      }
    }
  }
}
