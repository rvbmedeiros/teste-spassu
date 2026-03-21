import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import App from '@/App.vue'

describe('App', () => {
  it('renderiza a barra de navegacao e a area principal', () => {
    const wrapper = mount(App, {
      global: {
        stubs: {
          TheNavbar: { template: '<header data-test="navbar">navbar</header>' },
          RouterView: { template: '<section data-test="router-view">conteudo</section>' },
        },
      },
    })

    expect(wrapper.find('[data-test="navbar"]').exists()).toBe(true)
    expect(wrapper.find('main').exists()).toBe(true)
    expect(wrapper.find('[data-test="router-view"]').exists()).toBe(true)
  })
})