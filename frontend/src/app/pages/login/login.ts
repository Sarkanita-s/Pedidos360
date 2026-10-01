import { Component, OnInit, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MsalService } from '@azure/msal-angular';

@Component({
  selector: 'app-login',
  imports: [RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.scss'
})
export class Login implements OnInit {

  private readonly msalService = inject(MsalService);
  private readonly router = inject(Router);

  ngOnInit(): void {
    const account = this.msalService.instance.getActiveAccount()
      ?? this.msalService.instance.getAllAccounts()[0];

    if (account) {
      this.msalService.instance.setActiveAccount(account);
      this.router.navigate(['/dashboard']);
    }
  }

  onMicrosoftLogin(): void {
    this.msalService.loginRedirect({
      scopes: [
        'api://ac3261d7-bac7-48e8-b189-1d5b9c93c107/access_as_user'
      ]
    });
  }
}