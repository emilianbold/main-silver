#define MOO 3

class Foo {
    int boo;
public:
    Foo();
    Foo(int _boo);

    void doFoo(int moo);
};

Foo::Foo(): boo(0) {
}

Foo::Foo(int _boo) {
    boo = _boo;
}

void Foo::doFoo(int moo) {
    int goo = MOO;
    boo = moo + goo;
    int bar = 1;
    ::bar = ::bar + bar + 1;
    func(::bar);
}

int bar = 1;
void func(int bar) {}

#ifdef MOO

#  elif BOO

#if BOO != 0

#endif

# else

#ifndef INTERNAL

# endif

#endif

namespace N1
{
    int fooN1(int par0 /* = 0 */); // no highlighting
    int fooN1(int par0 /* = 0 */);

    int fooN1(int par0 /* = 0 */) {

    }


    class AAA {
        void const_fun(int i) ;
        void const_fun(int i) const ;
    };


    void AAA::const_fun(int i) {

    }

    void AAA::const_fun(int i) const {

    }
}

struct A {
    int a;
    A(int i) {
        a = i;
    }
};

int main() {
    A a(1);
    a.a++;
}

void stringsTest() {
    char* ss = "string literal";    

    return 'char literal';
}

#define STR "string literal"

#define CMD 'char literal'

void charTest() {
    char* ss = 'char literal';    

    return "string literal";
}

struct NameId {

};

// Name Table
class NameTable
{
public:
    NameId AddSymbol();
    NameId AddSymbol(const std::string &s);
    NameId AddSymbol(const std::string &s, const std::string &busHead, 
                    int index1, int index2, const std::string &busTail);
    NameId AddSymbol(const std::string &s, const std::vector<std::string>
&bits);
    void AddTable (const NameTable &other);
}; // class NameTabl

NameId NameTable::AddSymbol()
{

}

NameId NameTable::AddSymbol(const std::string &s)
{

}

NameId NameTable::AddSymbol(const std::string &s, const std::string &busHead,
                            int index1, int index2, const std::string &busTail)
{

}

NameId NameTable::AddSymbol(const std::string &s, const std::vector<std::string> &bits)
{

}

void NameTable::AddTable (const NameTable &other) {
    NameId oneParam = other.AddSymbol("Default");
    NameId empty = other.AddSymbol();
    NameId twoParams = other.AddSymbol("Default", vector<std::string>());
    NameId moreParams = other.AddSymbol("Default", "second", 1, 3, "tree");

}
