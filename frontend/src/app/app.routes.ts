import { Routes } from '@angular/router';
import { MsalGuard } from '@azure/msal-angular';

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
        path: 'login',
        component: Login
    },
    {
        path: 'dashboard',
        component: Dashboard,
        canActivate: [MsalGuard]
    },
    {
        path: 'catalog',
        component: Catalog,
        canActivate: [MsalGuard]
    },
    {
        path: 'orders',
        component: Orders,
        canActivate: [MsalGuard]
    }
];