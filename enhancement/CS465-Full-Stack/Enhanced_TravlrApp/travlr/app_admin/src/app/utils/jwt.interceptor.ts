import { Injectable, Provider } from "@angular/core";
import { HttpRequest, HttpHandler, HttpEvent } from "@angular/common/http";
import { HttpInterceptor, HTTP_INTERCEPTORS } from "@angular/common/http";
import { Observable } from "rxjs";
import { AuthenticationService } from "../authentication.service";


/**
 *  First we define a boolean flag to identify wheather the URL belongs
 *  to on of the two AuthAPI URLs that we do not want to try and intercept.
 *  We then grab the JWT from the AuthenticationService clone the http request,
 *  inject the new Authorization header, and then handle the cloned request.
 * 
 *  We use Injectable and Provicer to provice the capabilities to change
 *  access and modify data in a pipeline. Observable is required as - 
 *  RESTful API and http interaction is asynchronous.
 */
@Injectable()
export class JwtInterceptor implements HttpInterceptor {
  constructor(
    private authenticationService: AuthenticationService
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler) :
      Observable<HttpEvent<any>> {
    var isAuthApi: boolean;
    //consol.log('Interceptor::URL' + request.url);
    if(request.url.startsWith('login') ||
      request.url.startsWith('register')) {
        isAuthApi = true;
      }
    else {
      isAuthApi = false;
    }

    if(this.authenticationService.isLoggedIn() && !isAuthApi) {
      let token = this.authenticationService.getToken();
      // console.log(token);
      const authReq = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
      return next.handle(authReq);
    }
    return next.handle(request);
  }
}

export const authInterceptProvider: Provider =
  { provide: HTTP_INTERCEPTORS,
      useClass: JwtInterceptor, multi: true };
