import { createRouter, createWebHistory } from 'vue-router'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/livros',
    },
    {
      path: '/livros',
      name: 'livros',
      component: () => import('@/views/LivrosView.vue'),
    },
    {
      path: '/autores',
      name: 'autores',
      component: () => import('@/views/AutoresView.vue'),
    },
    {
      path: '/assuntos',
      name: 'assuntos',
      component: () => import('@/views/AssuntosView.vue'),
    },
    {
      path: '/relatorio',
      name: 'relatorio',
      component: () => import('@/views/RelatorioView.vue'),
    },
    {
      path: '/flowcockpit',
      name: 'flowcockpit',
      component: () => import('@/views/FlowCockpitView.vue'),
    },
    {
      path: '/logs',
      name: 'logs',
      component: () => import('@/views/LogsView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/livros',
    },
  ],
})
