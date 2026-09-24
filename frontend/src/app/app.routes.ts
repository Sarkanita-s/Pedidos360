import { Routes } from '@angular/router';
import { Catalog } from './pages/catalog/catalog';
import { Orders } from './pages/orders/orders';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'catalog',
        pathMatch: 'full'
    },
    {
        path: 'catalog',
        component: Catalog
    },
    {
        path: 'orders',
        component: Orders
    }
];