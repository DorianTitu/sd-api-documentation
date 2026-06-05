package SecurityData.api_documentation.security;

import SecurityData.api_documentation.enums.TipoUsuario;
import SecurityData.api_documentation.model.Usuario;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class UsuarioPrincipal implements UserDetails {

    private final Long id;
    private final String correo;
    private final String clave;
    private final String nombre;
    private final TipoUsuario tipoUsuario;
    private final List<GrantedAuthority> authorities;

    public UsuarioPrincipal(Usuario usuario) {
        this.id = usuario.getId();
        this.correo = usuario.getCorreo();
        this.clave = usuario.getClave();
        this.nombre = usuario.getNombre();
        this.tipoUsuario = usuario.getTipoUsuario();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getTipoUsuario().name()));
    }

    public Long getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return clave;
    }

    @Override
    public String getUsername() {
        return correo;
    }
}
