import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MsalService } from '@azure/msal-angular';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App implements OnInit {

  private readonly msalService = inject(MsalService);

  ngOnInit(): void {
    this.msalService.handleRedirectObservable().subscribe({
      next: (result) => {
        if (result?.account) {
          this.msalService.instance.setActiveAccount(result.account);
        }

        const accounts =
          this.msalService.instance.getAllAccounts();

        if (
          !this.msalService.instance.getActiveAccount() &&
          accounts.length > 0
        ) {
          this.msalService.instance.setActiveAccount(accounts[0]);
        }
      },
      error: (error) => {
        console.error(
          'Error procesando autenticación de Microsoft:',
          error
        );
      }
    });
  }
}