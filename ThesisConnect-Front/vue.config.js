const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  lintOnSave: false,
  devServer: {
    port: 3030
  },
  chainWebpack: config => {
    config.plugin('html').tap(args => {
      args[0].title = '毕业论文选题双向选择系统'
      return args
    })
  }
})
