import { Routes } from '@angular/router';
import { Catalog } from './pages/catalog/catalog';
import { Orders } from './pages/orders/orders';
import { Dashboard } from './pages/dashboard/dashboard';
import { Login } from './pages/login/login';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'dashboard',
        pathMatch: 'full'
    },
    {
        path: 'dashboard',
        component: Dashboard
    },
    {
        path: 'catalog',
        component: Catalog
    },
    {
        path: 'orders',
        component: Orders
    },
    {
        path: 'login',
        component: Login
    }
];